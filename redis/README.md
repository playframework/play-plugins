# Redis Plugin

This plugin provides support for [Redis](http://redis.io/) using the best Java driver [Jedis](https://github.com/xetorthio/jedis) and the corresponding Scala wrapper [Sedis](https://github.com/pk11/sedis). Also implements play's internal [Caching] (https://github.com/playframework/Play20/blob/master/framework/src/play/src/main/scala/play/api/cache/Cache.scala#L9) interface  

# Features

###  Provides a Redis-based Cache API (supported types: String, Int, Long, Boolean and Serializable) ie.

```java
//java
String f = (String) play.cache.Cache.get("mykey");
```

and 

```scala
//scala
val o = play.api.cache.Cache.getAs[String]("mykey")
```

#### Configurable

* Point to your Redis server using configuration settings  ```redis.host```, ```redis.port```,  ```redis.password``` and ```redis.database``` (defaults: ```localhost```, ```6379```, ```null``` and ```0```).
* Alternatively, specify a URI-based configuration using ```redis.uri``` (for example: ```redis.uri="redis://user:password@localhost:6379"```).
* Configure your Sentinels using ```redis.master.name``` and ```redis.sentinel.hosts``` (defaults: ```mymaster```, ```localhost:26379```).
* If ```redis.<name>.sentinel.mode``` is true (default: ```false```), then the ```<name>``` named cached will always contact the master node. Otherwise, the ```redis.host```, ```redis.port```, or ```redis.uri``` settings will be used.
* Set the timeout in milliseconds using ```redis.timeout``` (default is 2000).
* Configure any aspect of the connection pool. See [the documentation for commons-pool2 ```GenericObjectPoolConfig```](https://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool2/impl/GenericObjectPoolConfig.html), the underlying pool implementation, for more information on each setting.
    * redis.pool.maxIdle
    * redis.pool.minIdle
    * redis.pool.maxTotal
    * redis.pool.maxWaitMillis
    * redis.pool.testOnBorrow
    * redis.pool.testOnReturn
    * redis.pool.testWhileIdle
    * redis.pool.timeBetweenEvictionRunsMillis
    * redis.pool.numTestsPerEvictionRun
    * redis.pool.minEvictableIdleTimeMillis
    * redis.pool.softMinEvictableIdleTimeMillis
    * redis.pool.lifo
    * redis.pool.blockWhenExhausted


#### Allows direct access to Jedis and Sedis: 
play = 2.3.x:
```java
//java
import com.typesafe.plugin.RedisPlugin;
import redis.clients.jedis.*;

Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();

try {
  /// ... do stuff here 
  j.set("foo", "bar");
} finally {
  play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
}  
```

```scala
//scala
import play.api.Play.current
import com.typesafe.plugin.use
import com.typesafe.plugin.RedisPlugin

val pool = use[RedisPlugin].sedisPool
pool.withJedisClient { client =>
  Option[String] single = Dress.up(client).get("single")
}
```
play = 2.4.x and 2.5.x:
Because the underlying Sedis and Sentinel Pool was injected for the cache module to use, you can just inject the Pool yourself, something like this:

```scala
//scala
import javax.inject.Inject
import org.sedis.Pool

class TryIt @Inject()(sedisPool: Pool) extends Controller {
   val directValue: String = sedisPool.withJedisClient(client => client.get("someKey"))
}
```

```java
//java
import javax.inject.Inject
import redis.clients.jedis.JedisPool

class TryIt extends Controller {
   
   //The JedisPool will be injected for you from the module
   @Inject JedisPool jedisPool;

   ...
}
```

The Play 2.4.x and 2.5.x module also supports compile time DI via RedisCacheComponents. Mix this in with your custom application loader just like you would if you were using EhCacheComponents from the reference cache module.



# How to install

* add

play < 2.3.x:
```"com.typesafe" %% "play-plugins-redis" % "2.0.4"``` to your dependencies

play = 2.3.x:
```"com.typesafe.play.plugins" %% "play-plugins-redis" % "2.3.1"``` to your dependencies

* create a file called ```play.plugins``` in your ```conf``` directory

* add ```550:com.typesafe.plugin.RedisPlugin```

*  while this plugin is going to be loaded before the default cache implementation,  it's a good practice to disable the overwritten plugin:

 ```
 #conf/application.conf
 ehcacheplugin=disabled
 ```

play = 2.4.x:
```"com.typesafe.play.modules" %% "play-modules-redis" % "2.4.1"``` to your dependencies
and you'll probably need to add this resolver too to resolve Sedis (see [issue](https://github.com/typesafehub/play-plugins/issues/141)):
```resolvers += "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"```
* The default cache module (EhCache) will be used for all non-named cache UNLESS this module (RedisModule) is the only cache module that was loaded. If this module is the only cache module being loaded, it will work as expected on named and non-named cache. To disable the default cache module so that this Redis Module can be the default cache you must put this in your configuration:

 ```
 play.modules.disabled = ["play.api.cache.EhCacheModule"]
 ```

* This module supports play 2.4 NamedCaches through key namespacing on a single Sedis pool. To add additional namepsaces besides the default (play), the configuration would look like such:

 ```
 play.cache.redis.bindCaches = ["db-cache", "user-cache", "session-cache"]
 ```

play = 2.5.x:
```"com.typesafe.play.modules" %% "play-modules-redis" % "2.5.0"``` to your dependencies
and you'll probably need to add this resolver too to resolve Sedis (see [issue](https://github.com/typesafehub/play-plugins/issues/141)):
```resolvers += "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"```
* The default cache module (EhCache) will be used for all non-named cache UNLESS this module (RedisModule) is the only cache module that was loaded. If this module is the only cache module being loaded, it will work as expected on named and non-named cache. To disable the default cache module so that this Redis Module can be the default cache you must put this in your configuration:

 ```
 play.modules.disabled = ["play.api.cache.EhCacheModule"]
 ```

* This module supports play 2.5 NamedCaches through key namespacing on a single Sedis pool. To add additional namepsaces besides the default (play), the configuration would look like such:

 ```
 play.cache.redis.bindCaches = ["db-cache", "user-cache", "session-cache"]
 ```




## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
