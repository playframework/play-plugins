package com.typesafe.plugin

import play.api._
import org.sedis._
import redis.clients.jedis._
import play.api.cache._
import java.util._
import java.io._
import biz.source_code.base64Coder._

/**
 * provides a redis client and a CachePlugin implementation
 * the cache implementation can deal with the following data types:
 * - classes implement Serializable
 * - String, Int, Boolean and long
 */
class RedisPlugin(app: Application) extends CachePlugin {

 
 private lazy val host = app.configuration.getString("redis.host").getOrElse("localhost")
 private lazy val port = app.configuration.getInt("redis.port").getOrElse(6379)
 private lazy val timeout = app.configuration.getInt("redis.timeout").getOrElse(2000)

 /**
  * provides access to the underlying jedis Pool
  */
 lazy val jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout)

  /**
  * provides access to the sedis Pool
  */           
 lazy val sedisPool = new Pool(jedisPool)

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
     var oos: ObjectOutputStream = null
     var dos: DataOutputStream = null
     try { 
       val baos = new ByteArrayOutputStream()
       var prefix = "oos"
       if (value.getClass.isInstanceOf[Serializable]) {
          oos = new ObjectOutputStream(baos)
          oos.writeObject(value)
          oos.flush()
       } else if (value.getClass.isInstanceOf[String]) {
          dos = new DataOutputStream(baos)
          dos.writeUTF(value.asInstanceOf[String])
          prefix = "string"
       } else if (value.getClass.isInstanceOf[Int]) {
          dos = new DataOutputStream(baos)
          dos.writeInt(value.asInstanceOf[Int])
          prefix = "int"
       } else if (value.getClass.isInstanceOf[Long]) {
          dos = new DataOutputStream(baos)
          dos.writeLong(value.asInstanceOf[Long])
          prefix = "long"
       } else if (value.getClass.isInstanceOf[Boolean]) {
          dos = new DataOutputStream(baos)
          dos.writeBoolean(value.asInstanceOf[Boolean]) 
          prefix = "boolean"   
       } else {
          throw new IOException("could not serialize: "+ value.toString)
       }
       val redisV = prefix + "-" + new String( Base64Coder.encode( baos.toByteArray() ) )  
       Logger.warn(redisV)
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

    def get(key: String): Option[Any] = {
      var ois: ObjectInputStream = null
      var dis: DataInputStream = null
      try {
        val data: Seq[String] =  sedisPool.withJedisClient { client =>
            client.get(key)
        }.split("-")
        val b = Base64Coder.decode(data.last)
        data.head match {
          case "oos" => 
              ois = new ObjectInputStream(new ByteArrayInputStream(b))
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