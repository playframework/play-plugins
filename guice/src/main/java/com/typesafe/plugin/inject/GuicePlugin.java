package com.typesafe.plugin.inject;
import play.*;

import com.google.inject.*;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;

public class GuicePlugin extends InjectPlugin {
  
  private Injector injector = null;

  public GuicePlugin(Application app) {
    super(app);
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
        injector.injectMembers(createInstane(clazz));
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
  private Object createInstane(Class<Object> clazz) throws java.lang.IllegalArgumentException {
     Constructor constructor = null;
     for(Constructor<?> c : clazz.getConstructors()) {
       if(c.getParameterTypes().length == 0) {
          constructor = c;
          break; 
       }
     }
     try {
      return constructor.newInstance();
     } catch (Exception ex) {
       ex.printStackTrace();
       throw new java.lang.IllegalArgumentException();
     }
  } 
}