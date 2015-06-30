package com.typesafe.play.redis

import javax.inject.{Inject, Provider}

import org.sedis.Pool
import play.api.cache.{CacheApi, Cached, NamedCache}
import play.api.inject._
import play.api.{Configuration, Environment}
import play.cache.{CacheApi => JavaCacheApi, DefaultCacheApi => DefaultJavaCacheApi, NamedCacheImpl}
import redis.clients.jedis.JedisPool

/**
 * Redis cache components for compile time injection
 */
trait RedisCacheComponents {
  def environment: Environment
  def configuration: Configuration
  def applicationLifecycle: ApplicationLifecycle

  lazy val jedisPool: JedisPool = new JedisPoolProvider(configuration, applicationLifecycle).get
  lazy val sedisPool: Pool = new SedisPoolProvider(jedisPool).get

  /**
   * Use this to create with the given name.
   */
  def cacheApi(name: String): CacheApi = {
    new RedisCacheApi(name, sedisPool, environment.classLoader)
  }

  lazy val redisDefaultCacheApi: CacheApi = cacheApi(RedisModule.defaultCacheNameFromConfig(configuration))
}

class RedisModule extends Module {

  import scala.collection.JavaConversions._

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val ehcacheDisabled = configuration.getStringList("play.modules.disabled").fold(false)(x => x.contains("play.api.cache.EhCacheModule"))
    val defaultCacheName = RedisModule.defaultCacheNameFromConfig(configuration)
    val bindCaches = configuration.underlying.getStringList("play.cache.redis.bindCaches").toSeq

    // Creates a named cache qualifier
    def named(name: String): NamedCache = {
      new NamedCacheImpl(name)
    }

    // bind a cache with the given name
    def bindCache(name: String) = {
      val namedCache = named(name)
      val cacheApiKey = bind[CacheApi].qualifiedWith(namedCache)
      Seq(
        cacheApiKey.to(new NamedRedisCacheApiProvider(name, bind[Pool], environment.classLoader)),
        bind[JavaCacheApi].qualifiedWith(namedCache).to(new NamedJavaCacheApiProvider(cacheApiKey)),
        bind[Cached].qualifiedWith(namedCache).to(new NamedCachedProvider(cacheApiKey))
      )
    }

    val defaultBindings = Seq(
      bind[JedisPool].toProvider[JedisPoolProvider],
      bind[Pool].toProvider[SedisPoolProvider],
      bind[JavaCacheApi].to[DefaultJavaCacheApi]
    ) ++ bindCaches.flatMap(bindCache)

    // alias the default cache to the unqualified implementation only if the default cache is disabled as it already does this.
    if (ehcacheDisabled)
      Seq(bind[CacheApi].to(bind[CacheApi].qualifiedWith(named(defaultCacheName)))) ++ bindCache(defaultCacheName) ++ defaultBindings
    else
      defaultBindings
  }
}

object RedisModule {
  def defaultCacheNameFromConfig(configuration: Configuration): String = {
    configuration.underlying.getString("play.cache.defaultCache")
  }
}

class NamedRedisCacheApiProvider(namespace: String, client: BindingKey[Pool], classLoader: ClassLoader) extends Provider[CacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: CacheApi = {
    new RedisCacheApi(namespace, injector.instanceOf(client), classLoader)
  }
}

class NamedJavaCacheApiProvider(key: BindingKey[CacheApi]) extends Provider[JavaCacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: JavaCacheApi = {
    new DefaultJavaCacheApi(injector.instanceOf(key))
  }
}

class NamedCachedProvider(key: BindingKey[CacheApi]) extends Provider[Cached] {
  @Inject private var injector: Injector = _
  lazy val get: Cached = {
    new Cached(injector.instanceOf(key))
  }
}
