package com.typesafe.play.redis

import play.api.Configuration
import redis.clients.jedis.JedisPoolConfig

object PoolConfig {
  def createPoolConfig(config: Configuration): JedisPoolConfig = {
    val poolConfig: JedisPoolConfig = new JedisPoolConfig()
    config.getInt("redis.pool.maxIdle").foreach(poolConfig.setMaxIdle)
    config.getInt("redis.pool.minIdle").foreach(poolConfig.setMinIdle)
    config.getInt("redis.pool.maxTotal").foreach(poolConfig.setMaxTotal)
    config.getLong("redis.pool.maxWaitMillis").foreach(poolConfig.setMaxWaitMillis)
    config.getBoolean("redis.pool.testOnBorrow").foreach(poolConfig.setTestOnBorrow)
    config.getBoolean("redis.pool.testOnReturn").foreach(poolConfig.setTestOnReturn)
    config.getBoolean("redis.pool.testWhileIdle").foreach(poolConfig.setTestWhileIdle)
    config.getLong("redis.pool.timeBetweenEvictionRunsMillis").foreach(poolConfig.setTimeBetweenEvictionRunsMillis)
    config.getInt("redis.pool.numTestsPerEvictionRun").foreach(poolConfig.setNumTestsPerEvictionRun)
    config.getLong("redis.pool.minEvictableIdleTimeMillis").foreach(poolConfig.setMinEvictableIdleTimeMillis)
    config.getLong("redis.pool.softMinEvictableIdleTimeMillis").foreach(poolConfig.setSoftMinEvictableIdleTimeMillis)
    config.getBoolean("redis.pool.lifo").foreach(poolConfig.setLifo)
    config.getBoolean("redis.pool.blockWhenExhausted").foreach(poolConfig.setBlockWhenExhausted)
    poolConfig
  }
}
