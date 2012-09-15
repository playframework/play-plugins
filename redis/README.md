# Redis Plugin

This plugin provides support for [Redis](http://redis.io/) using the best Java driver [Jedis](https://github.com/xetorthio/jedis) and the corresponding Scala wrapper [Sedis](https://github.com/pk11/sedis). Also implements play's internal [Caching] (https://github.com/playframework/Play20/blob/master/framework/src/play/src/main/scala/play/api/cache/Cache.scala#L9) interface  

# Features

*  Provides a Redis-based Cache API (supported types: String, Int, Long, Boolean and Serializable) ie.

```java
//java
String f = (String) play.cache.Cache.get("mykey");
```

and 

```scala
//scala
val o = play.api.cache.Cache.getAs[String]("mykey")
```

* configurable ( variables: ```redis.host```, ```redis.port```, ```redis.timeout```, ```redis.password```, defaults are ```localhost```, ```6379```, ```2000```, ```nul``` )

* Allows direct access to Jedis and Sedis: 

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

play 2.0.1:
```"com.typesafe" %% "play-plugins-redis" % "2.0.1"``` to your dependencies

play 2.0.1:
```"com.typesafe" %% "play-plugins-redis" % "2.0.2"``` to your dependencies

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
