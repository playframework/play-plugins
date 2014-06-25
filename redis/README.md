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

* Point to your Redis server using configuration settings  ```redis.host```, ```redis.port```,  ```redis.password``` and ```redis.database``` (defaults: ```localhost```, ```6379```, ```null``` and ```0```)
* Alternatively, specify a URI-based configuration using ```redis.uri``` (for example: ```redis.uri="redis://user:password@localhost:6379"```).
* Set the timeout in milliseconds using ```redis.timeout``` (default is 2000).
* Configure any aspect of the connection pool. See [the documentation for commons-pool ```GenericObjectPool```](http://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool/impl/GenericObjectPool.html), the underlying pool implementation, for more information on each setting.
    * redis.pool.maxIdle
    * redis.pool.minIdle
    * redis.pool.maxActive
    * redis.pool.maxWait
    * redis.pool.testOnBorrow
    * redis.pool.testOnReturn
    * redis.pool.testWhileIdle
    * redis.pool.timeBetweenEvictionRunsMillis
    * redis.pool.numTestsPerEvictionRun
    * redis.pool.minEvictableIdleTimeMillis
    * redis.pool.softMinEvictableIdleTimeMillis
    * redis.pool.lifo
    * redis.pool.whenExhaustedAction (valid options: "fail", "block" (default), "grow")


#### Allows direct access to Jedis and Sedis: 

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
import com.typesafe.plugin.RedisPlugin

val pool = use[RedisPlugin].sedisPool
pool.withJedisClient { client =>
  Option[String] single = Dress.up(client).get("single")
}
```
# How to install

* add 

play 2.2.x:
```"com.typesafe" %% "play-plugins-redis" % "2.2.0"``` to your dependencies

* create a file called ```play.plugins``` in your ```app/conf``` directory

* add ```550:com.typesafe.plugin.RedisPlugin```

*  while this plugin is going to be loaded before the default cache implementation,  it's a good practice to disable the overwritten plugin:

```
#conf/application.conf
ehcacheplugin=disabled
```

# Sample

for an example, see the bundled sample app


## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
