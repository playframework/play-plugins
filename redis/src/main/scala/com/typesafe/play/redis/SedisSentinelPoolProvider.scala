package com.typesafe.play.redis

import javax.inject.{Inject, Provider, Singleton}

import org.sedis.SentinelPool
import redis.clients.jedis.JedisSentinelPool

@Singleton
class SedisSentinelPoolProvider @Inject()(jedisSentinelPool: JedisSentinelPool) extends Provider[SentinelPool] {
  lazy val get: SentinelPool = {
    val sedisSentinelPool = {
      new SentinelPool(jedisSentinelPool)
    }
    sedisSentinelPool
  }
}
