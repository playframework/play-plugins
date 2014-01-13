import play.*;
import play.libs.*;

import java.util.*;
import redis.clients.jedis.*;
import com.typesafe.plugin.RedisPlugin;
import play.cache.Cache;

public class Global extends GlobalSettings {
    
    public void onStart(Application app) {
      JedisPool p = app.plugin(RedisPlugin.class).jedisPool();
      // uncomment to test sentinel setup
      //JedisSentinelPool p = app.plugin(RedisPlugin.class).jedisSentinelPool();
      Jedis j = p.getResource();
      j.set("foo","yay");
      p.returnResource(j);
      Cache.set("foo2",5);
      Map<String, String> m = new HashMap<String,String>();
      m.put("test","value");
      Cache.set("foo3",m);
    }
}
