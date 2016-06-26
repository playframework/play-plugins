package com.typesafe.play.redis

import PoolConfig.createPoolConfig

import javax.inject.{Inject, Provider, Singleton}

import collection.JavaConverters._
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle
import redis.clients.jedis.JedisSentinelPool


import scala.concurrent.Future

@Singleton
class JedisSentinelPoolProvider @Inject()(config: Configuration, lifecycle: ApplicationLifecycle) extends Provider[JedisSentinelPool] {

  lazy val logger = Logger("redis.module")
  lazy val get: JedisSentinelPool = {
    val jedisSentinelPool = {
      val masterName = config.getString("redis.master.name").getOrElse("mymaster")

      val sentinelHosts = config.getStringList("redis.sentinel.hosts").getOrElse(Seq("localhost:26379").asJava)

      val sentinelSet = new java.util.HashSet[String]()
      sentinelSet.addAll(sentinelHosts)

      val password = config.getString("redis.password").orNull

      val timeout = config.getInt("redis.timeout").getOrElse(2000)

      val poolConfig = createPoolConfig(config)
      Logger.info(s"Redis Plugin enabled. Monitoring Redis master $masterName with Sentinels $sentinelSet and timeout $timeout.")
      Logger.info("Redis Plugin pool configuration: " + new ReflectionToStringBuilder(poolConfig).toString)

      new JedisSentinelPool(masterName, sentinelSet, poolConfig, timeout, password)
    }

    logger.info("Starting Jedis Sentinel Pool Provider")

    lifecycle.addStopHook(() => Future.successful {
      logger.info("Stopping Jedis Sentinel Pool Provider")
      jedisSentinelPool.destroy()
    })

    jedisSentinelPool
  }
}