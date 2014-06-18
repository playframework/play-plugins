package com.typesafe.plugin

import play.api._
import org.sedis._
import redis.clients.jedis._
import play.api.cache._
import java.util._
import java.io._
import java.net.URI
import biz.source_code.base64Coder._
import org.apache.commons.lang3.builder._
import org.apache.commons.pool.impl.GenericObjectPool
import play.api.mvc.Result
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * provides a redis client and a CachePlugin implementation
 * the cache implementation can deal with the following data types:
 * - classes implement Serializable
 * - String, Int, Boolean and long
 */
class RedisPlugin(app: Application) extends CachePlugin {

 private lazy val redisUri = app.configuration.getString("redis.uri").map { new URI(_) }

 private lazy val host = app.configuration.getString("redis.host")
                         .orElse(redisUri.map{_.getHost()})
                         .getOrElse("localhost")

 private lazy val port = app.configuration.getInt("redis.port")
                         .orElse(redisUri.map{_.getPort()}.filter{_ != -1})
                         .getOrElse(6379)

 private lazy val password = app.configuration.getString("redis.password")
                            .orElse(redisUri.map{ _.getUserInfo() }.filter{_ != null}.filter{ _ contains ":" }.map{_.split(":", 2)(1)})
                            .getOrElse(null)

 private lazy val timeout = app.configuration.getInt("redis.timeout")
                            .getOrElse(2000)

 private lazy val database = app.configuration.getInt("redis.database")
                          	.getOrElse(0)


 /**
  * provides access to the underlying jedis Pool
  */
 lazy val jedisPool = {
   val poolConfig = createPoolConfig(app)
   Logger.info(s"Redis Plugin enabled. Connecting to Redis on ${host}:${port} to ${database} with timeout ${timeout}.")
   Logger.info("Redis Plugin pool configuration: " + new ReflectionToStringBuilder(poolConfig).toString())
   new JedisPool(poolConfig, host, port, timeout, password, database)
 }

  /**
  * provides access to the sedis Pool
  */
 lazy val sedisPool = new Pool(jedisPool)

 private def createPoolConfig(app: Application) : JedisPoolConfig = {
   val poolConfig : JedisPoolConfig = new JedisPoolConfig()
   app.configuration.getInt("redis.pool.maxIdle").map { poolConfig.maxIdle = _ }
   app.configuration.getInt("redis.pool.minIdle").map { poolConfig.minIdle = _ }
   app.configuration.getInt("redis.pool.maxActive").map { poolConfig.maxActive = _ }
   app.configuration.getInt("redis.pool.maxWait").map { poolConfig.maxWait = _ }
   app.configuration.getBoolean("redis.pool.testOnBorrow").map { poolConfig.testOnBorrow = _ }
   app.configuration.getBoolean("redis.pool.testOnReturn").map { poolConfig.testOnReturn = _ }
   app.configuration.getBoolean("redis.pool.testWhileIdle").map { poolConfig.testWhileIdle = _ }
   app.configuration.getLong("redis.pool.timeBetweenEvictionRunsMillis").map { poolConfig.timeBetweenEvictionRunsMillis = _ }
   app.configuration.getInt("redis.pool.numTestsPerEvictionRun").map { poolConfig.numTestsPerEvictionRun = _ }
   app.configuration.getLong("redis.pool.minEvictableIdleTimeMillis").map { poolConfig.minEvictableIdleTimeMillis = _ }
   app.configuration.getLong("redis.pool.softMinEvictableIdleTimeMillis").map { poolConfig.softMinEvictableIdleTimeMillis = _ }
   app.configuration.getBoolean("redis.pool.lifo").map { poolConfig.lifo = _ }
    app.configuration.getString("redis.pool.whenExhaustedAction").map { setting =>
      poolConfig.whenExhaustedAction = setting match {
        case "fail"  | "0" => GenericObjectPool.WHEN_EXHAUSTED_FAIL
        case "block" | "1" => GenericObjectPool.WHEN_EXHAUSTED_BLOCK
        case "grow"  | "2" => GenericObjectPool.WHEN_EXHAUSTED_FAIL
      }
    }
   poolConfig
 }

 override def onStart() {
    sedisPool
 }

 override def onStop() {
    jedisPool.destroy()
 }

 override lazy val enabled = {
    !app.configuration.getString("redisplugin").filter(_ == "disabled").isDefined
  }

 /**
  * cacheAPI implementation
  * can serialize, deserialize to/from redis
  * value needs be Serializable (a few primitive types are also supported: String, Int, Long, Boolean)
  */
 lazy val api = new CacheAPI {

    def set(key: String, value: Any, expiration: Int) {
      value match {
        case result:Result =>
          RedisResult.wrapResult(result).map {
            redisResult => set_(key, redisResult, expiration)
          }
        case _ => set_(key, value, expiration)
      }
    }

    def set_(key: String, value: Any, expiration: Int) {
     var oos: ObjectOutputStream = null
     var dos: DataOutputStream = null
     try {
       val baos = new ByteArrayOutputStream()
       var prefix = "oos"
       if (value.isInstanceOf[RedisResult]) {
          oos = new ObjectOutputStream(baos)
          oos.writeObject(value)
          oos.flush()
          prefix = "result"
       } else if (value.isInstanceOf[Serializable]) {
          oos = new ObjectOutputStream(baos)
          oos.writeObject(value)
          oos.flush()
       } else if (value.isInstanceOf[String]) {
          dos = new DataOutputStream(baos)
          dos.writeUTF(value.asInstanceOf[String])
          prefix = "string"
       } else if (value.isInstanceOf[Int]) {
          dos = new DataOutputStream(baos)
          dos.writeInt(value.asInstanceOf[Int])
          prefix = "int"
       } else if (value.isInstanceOf[Long]) {
          dos = new DataOutputStream(baos)
          dos.writeLong(value.asInstanceOf[Long])
          prefix = "long"
       } else if (value.isInstanceOf[Boolean]) {
          dos = new DataOutputStream(baos)
          dos.writeBoolean(value.asInstanceOf[Boolean])
          prefix = "boolean"
       } else {
          throw new IOException("could not serialize: "+ value.toString)
       }
       val redisV = prefix + "-" + new String( Base64Coder.encode( baos.toByteArray() ) )
       Logger.trace(s"Setting key ${key} to ${redisV}")

       sedisPool.withJedisClient { client =>
          client.set(key,redisV)
          if (expiration != 0) client.expire(key,expiration)
       }
     } catch {case ex: IOException =>
       Logger.warn("could not serialize key:"+ key + " and value:"+ value.toString + " ex:"+ex.toString)
     } finally {
       if (oos != null) oos.close()
       if (dos != null) dos.close()
     }

    }
    def remove(key: String): Unit =  sedisPool.withJedisClient { client => client.del(key) }

    class ClassLoaderObjectInputStream(stream:InputStream) extends ObjectInputStream(stream) {
      override protected def resolveClass(desc: ObjectStreamClass) = {
        Class.forName(desc.getName(), false, app.classloader)
      }
    }

    def withDataInputStream[T](bytes: Array[Byte])(f: DataInputStream => T): T = {
      val dis = new DataInputStream(new ByteArrayInputStream(bytes))
      try f(dis) finally dis.close()
    }

    def withObjectInputStream[T](bytes: Array[Byte])(f: ObjectInputStream => T): T = {
      val ois = new ClassLoaderObjectInputStream(new ByteArrayInputStream(bytes))
      try f(ois) finally ois.close()
    }

    def get(key: String): Option[Any] = {
      Logger.trace(s"Reading key ${key}")

      try {
        val rawData = sedisPool.withJedisClient { client => client.get(key) }
        rawData match {
          case null =>
            None
          case _ =>
            val data: Seq[String] =  rawData.split("-")
            val bytes = Base64Coder.decode(data.last)
            data.head match {
              case "result" =>
                Some(RedisResult.unwrapResult(withObjectInputStream(bytes)(_.readObject())
                  .asInstanceOf[RedisResult]))
              case "oos" => Some(withObjectInputStream(bytes)(_.readObject()))
              case "string" => Some(withDataInputStream(bytes)(_.readUTF()))
              case "int" => Some(withDataInputStream(bytes)(_.readInt()))
              case "long" => Some(withDataInputStream(bytes)(_.readLong()))
              case "boolean" => Some(withDataInputStream(bytes)(_.readBoolean()))
              case _ => throw new IOException("can not recognize value")
            }
        }
      } catch {case ex: Exception =>
        Logger.warn("could not deserialize key:"+ key+ " ex:"+ex.toString)
        ex.printStackTrace()
        None
      }
    }

  }
}
