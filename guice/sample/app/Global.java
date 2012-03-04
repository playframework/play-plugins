import play.*;
import play.libs.*;

import java.util.*;

import service.Service;
import com.typesafe.plugin.inject.GuicePlugin;

public class Global extends GlobalSettings {
    
    public void onStart(Application app) {
      Logger.warn("getting an instance from guice:"+ app.plugin(GuicePlugin.class).getInstance(Service.class));
    }
}
