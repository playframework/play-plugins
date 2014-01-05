package com.typesafe.plugin

import play.api._
import org.sedis._
import redis.clients.jedis._
import play.api.cache._
import java.io._
import java.net.URI
import biz.source_code.base64Coder._
import org.apache.commons.lang3.builder._
import play.api.mvc.Result
import scala.collection.JavaConversions._

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

 private lazy val sentinelMode = app.configuration.getBoolean("redis.sentinel.mode")
                                 .getOrElse(false)

 private lazy val sentinelHosts : java.util.List[String] = app.configuration.getStringList("redis.sentinel.hosts")
                                                           .getOrElse(seqAsJavaList(List("localhost:6379")))

 private lazy val masterName = app.configuration.getString("redis.master.name")
                               .getOrElse("mymaster")

 /**
  * provides access to the underlying jedis Pool
  */
 lazy val jedisPool = {
   val poolConfig = createPoolConfig(app)
   Logger.info(s"Redis Plugin enabled. Connecting to Redis on ${host}:${port} with timeout ${timeout}.")
   Logger.info("Redis Plugin pool configuration: " + new ReflectionToStringBuilder(poolConfig).toString())
   new JedisPool(poolConfig, host, port, timeout, password)
 }

  /**
  * provides access to the sedis Pool
  */
 lazy val sedisPool = new Pool(jedisPool)

 /**
  * provides access to the underlying jedis sentinel Pool
  */
 lazy val jedisSentinelPool = {
   val poolConfig = createPoolConfig(app)
   Logger.info(s"Redis Plugin enabled. Connecting to Redis sentinels ${sentinelHosts} with timeout ${timeout}.")
   Logger.info("Redis Plugin pool configuration: " + new ReflectionToStringBuilder(poolConfig).toString())
   val sentinelSet = new java.util.HashSet[String]()
   sentinelSet.addAll(sentinelHosts)
   new JedisSentinelPool(masterName, sentinelSet, poolConfig, timeout, password)
 }

 /**
  * provides access to the sedis sentinel Pool
  */
 lazy val sedisSentinelPool = new SentinelPool(jedisSentinelPool)

 private def createPoolConfig(app: Application) : JedisPoolConfig = {
   val poolConfig : JedisPoolConfig = new JedisPoolConfig()
   app.configuration.getInt("redis.pool.maxIdle").map { poolConfig.setMaxIdle(_) }
   app.configuration.getInt("redis.pool.minIdle").map { poolConfig.setMinIdle(_) }
   app.configuration.getInt("redis.pool.maxTotal").map { poolConfig.setMaxTotal(_) }
   app.configuration.getBoolean("redis.pool.testOnBorrow").map { poolConfig.setTestOnBorrow(_) }
   app.configuration.getBoolean("redis.pool.testOnReturn").map { poolConfig.setTestOnReturn(_) }
   app.configuration.getBoolean("redis.pool.testWhileIdle").map { poolConfig.setTestWhileIdle(_) }
   app.configuration.getLong("redis.pool.timeBetweenEvictionRunsMillis").map { poolConfig.setTimeBetweenEvictionRunsMillis(_) }
   app.configuration.getInt("redis.pool.numTestsPerEvictionRun").map { poolConfig.setNumTestsPerEvictionRun(_) }
   app.configuration.getLong("redis.pool.minEvictableIdleTimeMillis").map { poolConfig.setMinEvictableIdleTimeMillis(_) }
   app.configuration.getLong("redis.pool.softMinEvictableIdleTimeMillis").map { poolConfig.setSoftMinEvictableIdleTimeMillis(_) }
   app.configuration.getBoolean("redis.pool.lifo").map { poolConfig.setLifo(_) }
   app.configuration.getBoolean("redis.pool.blockWhenExhausted").map { poolConfig.setBlockWhenExhausted(_) }
   poolConfig
 }

 override def onStart() {
   if (sentinelMode) {
     sedisSentinelPool
   } else {
     sedisPool
   }
 }

 override def onStop() {
   if (sentinelMode) {
     jedisSentinelPool.destroy()
   } else {
     jedisPool.destroy()
   }
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
     var oos: ObjectOutputStream = null
     var dos: DataOutputStream = null
     try {
       val baos = new ByteArrayOutputStream()
       var prefix = "oos"
       if (value.isInstanceOf[Result]) {
          oos = new ObjectOutputStream(baos)
          oos.writeObject(RedisResult.wrapResult(value.asInstanceOf[Result]))
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

       if (sentinelMode) {
         sedisSentinelPool.withJedisClient { client => setValue(client, key, redisV, expiration) }
       } else {
         sedisPool.withJedisClient { client => setValue(client, key, redisV, expiration) }
       }
     } catch {case ex: IOException =>
       Logger.warn("could not serialize key:"+ key + " and value:"+ value.toString + " ex:"+ex.toString)
     } finally {
       if (oos != null) oos.close()
       if (dos != null) dos.close()
     }

    }

    private def setValue(client: Jedis, key: String, value: String, expiration: Int) {
      client.set(key, value)
      if (expiration != 0) client.expire(key, expiration)
    }

    def remove(key: String): Unit = {
      if (sentinelMode) {
        sedisSentinelPool.withJedisClient { client => client.del(key) }
      } else {
        sedisPool.withJedisClient { client => client.del(key) }
      }
    }

    class ClassLoaderObjectInputStream(stream:InputStream) extends ObjectInputStream(stream) {
      override protected def resolveClass(desc: ObjectStreamClass) = {
        Class.forName(desc.getName(), false, play.api.Play.current.classloader)
      }
    }

    def get(key: String): Option[Any] = {
      Logger.trace(s"Reading key ${key}")
      
      var ois: ObjectInputStream = null
      var dis: DataInputStream = null
      try {
        val rawData = {
          if (sentinelMode) {
            sedisSentinelPool.withJedisClient { client => client.get(key) }
          } else {
            sedisPool.withJedisClient { client => client.get(key) }
          }
        }
        rawData match {
            case null =>
                None
            case _ =>
                val data: Seq[String] =  rawData.split("-")
                val b = Base64Coder.decode(data.last)
                data.head match {
                  case "result" =>
                      ois = new ClassLoaderObjectInputStream(new ByteArrayInputStream(b))
                      val r  = ois.readObject()
                      Some(RedisResult.unwrapResult(r.asInstanceOf[RedisResult]))
                  case "oos" =>
                      ois = new ClassLoaderObjectInputStream(new ByteArrayInputStream(b))
                      val r  = ois.readObject()
                      Some(r)
                  case "string" =>
                      dis = new DataInputStream(new ByteArrayInputStream(b))
                      val r  = dis.readUTF()
                      Some(r)
                  case "int" =>
                      dis = new DataInputStream(new ByteArrayInputStream(b))
                       val r  = dis.readInt
                      Some(r)
                  case "long" =>
                      dis = new DataInputStream(new ByteArrayInputStream(b))
                      val r  = dis.readLong
                      Some(r)
                  case "boolean" =>
                      dis = new DataInputStream(new ByteArrayInputStream(b))
                      val r  = dis.readBoolean
                      Some(r)
                  case _ => throw new IOException("can not recognize value")
                }
        }
      } catch {case ex: Exception =>
        Logger.warn("could not deserialize key:"+ key+ " ex:"+ex.toString)
        ex.printStackTrace()
        None
      } finally {
       if (ois != null) ois.close()
       if (dis != null) dis.close()
      }
    }

  }
}
