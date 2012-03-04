import play.*;
import play.libs.*;

import java.util.*;

import service.MyService;
import com.typesafe.plugin.inject.InjectPlugin;

public class Global extends GlobalSettings {
    
    public void onStart(Application app) {
      Logger.warn("getting an instance from guice:"+ app.plugin(InjectPlugin.class).getInstance(MyService.class));
    }
}
