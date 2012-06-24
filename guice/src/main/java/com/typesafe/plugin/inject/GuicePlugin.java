package com.typesafe.plugin.inject;
import play.*;

import com.google.inject.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

public class GuicePlugin extends InjectPlugin {
  
  private Injector injector = null;

  public GuicePlugin(Application app) {
    super(app);
  }

  @Override
  public boolean enabled() {
    return !(app.configuration().getString("guiceplugin") != null && app.configuration().getString("guiceplugin").equals("disabled") );
  }

  public <T> T getInstance(Class<T> type) {
     if (injector == null) Logger.warn ("injector is null - perhaps plugin is not configured before GlobalPlugin or onStart was not called yet");
       
     return injector.getInstance(type);
  }

  @Override
  public void onStart() {
    
    Class<Object>[] injectables = scanInjectableClasses();
    //create injector with static support
    injector = Guice.createInjector(convertToModules(availableModules(), injectables));

    //inject
    for (Class<Object> clazz : injectables ) {
      try {
        Logger.debug("injection for "+ clazz.getName());
        injector.injectMembers(createOrGetInstane(clazz));
      } catch (java.lang.IllegalArgumentException ex) {
        Logger.debug("skipping injection for "+ clazz.getName());
      } 
    }
  }

  /**
   * converts modules into Guice modules, also adding Static Injection support
   */
  private List<Module> convertToModules(List<Object> modules, Class<Object>[] injectables) {
    List<Module> guiceModules = new ArrayList<Module>();

    guiceModules.add(new RequestStaticInjection(injectables));
    
    for (Object m : modules ) {
      guiceModules.add((Module)m);
    }
    
    return guiceModules;
  }

  /**
   * creates instance for default constructor
   */
  private Object createOrGetInstane(Class<Object> clazz) throws java.lang.IllegalArgumentException {
     try {
       try {
         return clazz.newInstance();
       } catch (IllegalAccessException ex) {
         Field field = clazz.getField("MODULE$");
         return field.get(null);
       }
     } catch (Exception ex) {
       ex.printStackTrace();
       throw new java.lang.IllegalArgumentException();
     }
  } 
}
