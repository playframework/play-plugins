package com.typesafe.plugin.inject;
import play.*;

public class InjectViaMiniGuicePlugin extends InjectPlugin {
  
  public InjectViaMiniGuicePlugin(Application app) {
    super(app);
  }

  public <T> T getInstance(Class<T> type) {
    return com.google.inject.mini.MiniGuice.inject(type, true, availableModules().toArray());
  }

  @Override
  public void onStart() {
    //inject
    for (Class<Object> clazz : scanInjectableClasses() ) {
      try {
        Logger.debug("injection for "+ clazz.getName());
        com.google.inject.mini.MiniGuice.inject(clazz, true, availableModules().toArray());
      } catch (java.lang.IllegalArgumentException ex) {
        Logger.debug("skipping injection for "+ clazz.getName());
      }
    }
  }
}