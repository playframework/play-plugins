package module;

import com.google.inject.Provides;
import javax.inject.Singleton;
import service.*;
import play.Play;
import com.typesafe.plugin.inject.InjectPlugin;

public class Dependencies {
  
  public static InjectPlugin inject() {
    return Play.application().plugin(InjectPlugin.class);
  }  

  public static controllers.Application application() {
    return inject().getInstance(controllers.Application.class);
  }

  @Provides 
  @Singleton
  public Something makeSomething() { 
    return new Something() {
      public String noWhat() {
        return "yay";
      }
    };
  }

  @Provides 
  @Singleton
  public MyService makeService(Something s) {
    return new MyService(s) {
      public String demonstrate() { return s.noWhat();}
    };
  }


}