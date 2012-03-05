package com.typesafe.plugin

import play.api._
import org.sedis._
import redis.clients.jedis._
import play.api.cache._
import java.io._

class RedisPlugin(app: Application) extends Plugin {

 
 private lazy val host = app.configuration.getString("redis.host").getOrElse("localhost")
 private lazy val port = app.configuration.getInt("redis.port").getOrElse(6379)
 private lazy val timeout = app.configuration.getInt("redis.timeout").getOrElse(2000)

 private lazy val jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout)
 private lazy val sedisPool = new Pool(jedisPool)

 override def onStart() {
    sedisPool
 }

 override def onStop() {
    jedisPool.destroy()
 }

 override lazy val enabled = {
    !app.configuration.getString("redisplugin").filter(_ == "disabled").isDefined
  }

 lazy val api = sedisPool

 lazy val pool = jedisPool

}

class RedisCache (app: Application) extends CachePlugin {
  private lazy val redis = app.plugin[RedisPlugin].getOrElse{
    new RedisPlugin(app)
  }

  lazy val api = new CacheAPI {

    def set(key: String, value: Any, expiration: Int) {
     val out = new ByteArrayOutputStream()
     val oos = new ObjectOutputStream(out)
     oos.writeObject(value)
     oos.flush()
     oos.close()
     val redisV = out.toString("UTF-16LE")
     redis.api.withJedisClient { client =>
        client.set(key,redisV)
        client.expire(key,expiration)
     }
    
    }

    def get(key: String): Option[Any] = {
      try {
        val b = redis.api.withJedisClient { client =>
          client.get(key)
        }.getBytes("UTF-16LE")
        val in = new ByteArrayInputStream(b)
        val ois = new ObjectInputStream(in)
        val r = ois.readObject()
        ois.close()
        Some(r)
      } catch {case ex: Exception => 
        Logger.warn("could not desiralize key:"+ key+ " ex:"+ex.toString)
        None
      }
    }

  }
}


