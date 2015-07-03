package com.typesafe.play.redis

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

import org.sedis.Pool
import org.specs2.specification.AfterAll
import play.api.ApplicationLoader.Context
import play.api._
import play.api.cache.{CacheApi, Cached}
import play.api.inject.BindingKey
import play.api.mvc.{Action, Results}
import play.api.routing.Router
import play.api.routing.sird._
import play.api.test._
import play.cache.{NamedCache, NamedCacheImpl}

class RedisCachedSpec extends PlaySpecification with AfterAll {

  sequential

  val redisOnlyCache: (() => FakeApplication) = { () => FakeApplication(additionalConfiguration = Map(
    "play.modules.disabled" -> Seq("play.api.cache.EhCacheModule")
  ))
  }

  val multiCache: (() => FakeApplication) = { () => FakeApplication(additionalConfiguration = Map(
    "play.cache.redis.bindCaches" -> Seq("redis-test", "redis-results"),
    "play.cache.bindCaches" -> Seq("ehcache-test")
  ))
  }

  "The cached action" should {
    "cache values using injected Redis CachedApi" in new WithApplication(redisOnlyCache()) {
      val controller = app.injector.instanceOf[CachedController]

      val result1 = controller.action(FakeRequest()).run
      contentAsString(result1) must_== "1"
      controller.invoked.get() must_== 1

      val result2 = controller.action(FakeRequest()).run
      contentAsString(result2) must_== "1"
      controller.invoked.get() must_== 1

      // Test that the same headers are added
      header(ETAG, result2) must_== header(ETAG, result1)
      header(EXPIRES, result2) must_== header(EXPIRES, result1)
    }

    "cache values using named injected Redis CachedApi" in new WithApplication(multiCache()) {
      val controller = app.injector.instanceOf[NamedCacheController]

      val result1 = controller.action(FakeRequest()).run
      contentAsString(result1) must_== "1"
      controller.invoked.get() must_== 1
      val result2 = controller.action(FakeRequest()).run
      contentAsString(result2) must_== "1"
      controller.invoked.get() must_== 1

      // Test that the same headers are added
      header(ETAG, result2) must_== header(ETAG, result1)
      header(EXPIRES, result2) must_== header(EXPIRES, result1)

      // Test that the values are in the right cache
      app.injector.instanceOf[CacheApi].get("foo") must beNone
      controller.isCached("foo-etag") must beTrue
    }

    "support compile time DI" in new WithApplicationLoader(applicationLoader = new CompileTimeLoader) {
      val result1 = route(FakeRequest(GET, "/compileTime")).get
      status(result1) must_== OK
      contentAsString(result1) must_== "1"

      val result2 = route(FakeRequest(GET, "/compileTime")).get
      status(result2) must_== OK
      contentAsString(result2) must_== "1"

      // Test that the same headers are added
      header(ETAG, result2) must_== header(ETAG, result1)
      header(EXPIRES, result2) must_== header(EXPIRES, result1)
    }
  }

  "RedisModule" should {
    "assume default cache if reference impl is disabled" in new WithApplication(redisOnlyCache()) {
      val defaultCache = app.injector.instanceOf[CacheApi]
      defaultCache.set("default-foo", "bar")
      defaultCache.get("default-foo") must beSome("bar")
    }

    "support binding multiple different caches (namespaces) and cache implementations" in new WithApplication(multiCache()) {
      val defaultCache = app.injector.instanceOf[CacheApi]
      val namedEhCache = app.injector.instanceOf(BindingKey(classOf[CacheApi]).qualifiedWith(new NamedCacheImpl("ehcache-test")))
      val redisCache1 = app.injector.instanceOf(BindingKey(classOf[CacheApi]).qualifiedWith(new NamedCacheImpl("redis-test")))
      val redisCache2 = app.injector.instanceOf(BindingKey(classOf[CacheApi]).qualifiedWith(new NamedCacheImpl("redis-results")))

      defaultCache.set("default-foo", "bar")
      namedEhCache.get("default-foo") must beNone
      redisCache1.get("default-foo") must beNone
      redisCache2.get("default-foo") must beNone
      defaultCache.get("default-foo") must beSome("bar")

      namedEhCache.set("eh-foo", "zzzz")
      defaultCache.get("eh-foo") must beNone
      redisCache1.get("eh-foo") must beNone
      redisCache2.get("eh-foo") must beNone
      namedEhCache.get("eh-foo") must beSome("zzzz")

      redisCache1.set("cache1-foo", "buzz")
      defaultCache.get("cache1-foo") must beNone
      namedEhCache.get("cache1-foo") must beNone
      redisCache2.get("cache1-foo") must beNone
      redisCache1.get("cache1-foo") must beSome("buzz")

      redisCache2.set("cache2-foo", "zing")
      defaultCache.get("cache2-foo") must beNone
      namedEhCache.get("cache2-foo") must beNone
      redisCache1.get("cache2-foo") must beNone
      redisCache2.get("cache2-foo") must beSome("zing")
    }
  }

  "RedisCacheApi" should {
    "support object caching" in new WithApplication(redisOnlyCache()) {
      val cache = app.injector.instanceOf[CacheApi]

      val obj = ObjectTest(Seq("test"))
      cache.set("object-test", obj)
      cache.get("object-test") must beSome(obj)
    }

    "support string caching" in new WithApplication(redisOnlyCache()) {
      val cache = app.injector.instanceOf[CacheApi]

      val testValue = "my-string"
      cache.set("string-test", testValue)
      cache.get("string-test") must beSome(testValue)
    }

    "support int primitive caching" in new WithApplication(redisOnlyCache()) {
      val cache = app.injector.instanceOf[CacheApi]

      val testValue = 5
      cache.set("int-test", testValue)
      cache.get("int-test") must beSome(testValue)
    }

    "support long primitive caching" in new WithApplication(redisOnlyCache()) {
      val cache = app.injector.instanceOf[CacheApi]

      val testValue = 5L
      cache.set("long-test", testValue)
      cache.get("long-test") must beSome(testValue)
    }

    "support boolean primitive caching" in new WithApplication(redisOnlyCache()) {
      val cache = app.injector.instanceOf[CacheApi]

      val testValue = true
      cache.set("bool-test", testValue)
      cache.get("bool-test") must beSome(testValue)
    }

  }

  override def afterAll() = {
    redisOnlyCache().injector.instanceOf[Pool].withJedisClient(client => client.flushAll())
  }
}

case class ObjectTest(values: Seq[String]) extends Serializable

class SomeComponent @Inject()(@NamedCache("redis-test") cache: CacheApi) {
  def get(key: String) = cache.get[String](key)

  def set(key: String, value: String) = cache.set(key, value)
}

class CachedController @Inject()(cached: Cached) {
  val invoked = new AtomicInteger()
  val action = cached(_ => "foo")(Action(Results.Ok("" + invoked.incrementAndGet())))
}

class NamedCacheController @Inject()(@NamedCache("redis-results") cached: Cached, @NamedCache("redis-results") cache: CacheApi) extends CachedController(cached) {
  def isCached(key: String): Boolean = cache.get[String](key).isDefined
}

class CompileTimeLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    new CompileTimeAppComponents(context).application
  }
}

class CompileTimeAppComponents(context: Context) extends BuiltInComponentsFromContext(context) with RedisCacheComponents {

  lazy val cached: Cached = new Cached(redisDefaultCacheApi)

  override def router: Router = Router.from {
    case GET(p"/compileTime") => cached("redis-ci-cached") {
      Action {
        val invoked = new AtomicInteger()
        Results.Ok("" + invoked.incrementAndGet())
      }
    }
  }
}
