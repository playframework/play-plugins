package com.typesafe.play.redis

import javax.inject.{Inject, Provider, Singleton}

import org.sedis.Pool
import redis.clients.jedis.JedisPool

@Singleton
class SedisPoolProvider @Inject()(jedisPool: JedisPool) extends Provider[Pool] {
  lazy val get: Pool = {
    val sedisPool = {
      new Pool(jedisPool)
    }
    sedisPool
  }
}
