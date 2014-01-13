package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import com.typesafe.plugin.RedisPlugin;
import play.cache.Cache;
import redis.clients.jedis.*;

public class Application extends Controller {
  
  public static Result index() {
    JedisPool p = Play.application().plugin(RedisPlugin.class).jedisPool();
    // uncomment to test sentinel setup
    //JedisSentinelPool p = Play.application().plugin(RedisPlugin.class).jedisSentinelPool();
    Jedis j = p.getResource();
    String r = j.get("foo") + " - foo2:" + j.get("foo2");
    p.returnResource(j);
    return ok(index.render("foo3:"+ Cache.get("foo3")+" foo2:"+Cache.get("foo2").toString() +" - redis:" + r ));
  }
  
}
