package module;

import com.google.inject.Provides;
import javax.inject.Singleton;
import service.*;

public class Dependencies {
  
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