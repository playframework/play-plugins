package com.typesafe.play.redis

import PoolConfig.createPoolConfig

import java.net.URI
import javax.inject.{Provider, Inject, Singleton}

import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import play.api.inject.ApplicationLifecycle
import play.api.{Logger, Configuration}
import redis.clients.jedis.JedisPool

import scala.concurrent.Future

@Singleton
class JedisPoolProvider @Inject()(config: Configuration, lifecycle: ApplicationLifecycle) extends Provider[JedisPool]{

  lazy val logger = Logger("redis.module")
  lazy val get: JedisPool = {
    val jedisPool = {
      val redisUri = config.getString("redis.uri").map(new URI(_))

      val host = config.getString("redis.host")
        .orElse(redisUri.map(_.getHost))
        .getOrElse("localhost")

      val port = config.getInt("redis.port")
        .orElse(redisUri.map(_.getPort).filter(_ != -1))
        .getOrElse(6379)

      val password = config.getString("redis.password")
        .orElse(redisUri.map(_.getUserInfo).filter(_ != null).filter(_ contains ":").map(_.split(":", 2)(1)))
        .orNull

      val timeout = config.getInt("redis.timeout")
        .getOrElse(2000)

      val database = config.getInt("redis.database")
        .getOrElse(0)

      val poolConfig = createPoolConfig(config)
      Logger.info(s"Redis Plugin enabled. Connecting to Redis on $host:$port to $database with timeout $timeout.")
      Logger.info("Redis Plugin pool configuration: " + new ReflectionToStringBuilder(poolConfig).toString)


      new JedisPool(poolConfig, host, port, timeout, password, database)
    }

    logger.info("Starting Jedis Pool Provider")

    lifecycle.addStopHook(() => Future.successful {
      logger.info("Stopping Jedis Pool Provider")
      jedisPool.destroy()
    })

    jedisPool
  }
}
