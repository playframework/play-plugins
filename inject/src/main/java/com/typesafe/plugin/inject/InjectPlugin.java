package com.typesafe.plugin.inject;

import play.*;
import java.util.ArrayList;
import java.util.List;
public abstract class InjectPlugin extends Plugin {

  protected Application app;

  private final String scannedPackage;

  private final String moduleNames;

  private final String filter;

  private ArrayList<Object> _modules = null;

  public InjectPlugin(Application app) {
    this.app = app;
    scannedPackage = app.configuration().getString("inject.package");
    moduleNames = app.configuration().getString("inject.modules");
    filter = app.configuration().getString("inject.filter");
  }

  abstract public <T> T getInstance(Class<T> type);
  
  protected List<Object> availableModules() {
    if ( _modules == null) {
      _modules = createModules();
    }
    return _modules;
  }

  /**
   * by default select only play.mvc.Controllers
   */
  @SuppressWarnings(value = "unchecked")  
  protected  Class<Object>[] selectClasses(Class[] fullClassList) {
    ArrayList<Class<Object>> classNames = new ArrayList<Class<Object>>();
    if (filter == null) {
      for (Class c: fullClassList) {
        if (play.mvc.Controller.class.isAssignableFrom(c)) {
          classNames.add(c);
        }
      }
    } else {
      try {
        Class clazz = Class.forName(filter);
         for (Class c: fullClassList) {
            if (clazz.isAssignableFrom(c)) {
              classNames.add(c);
            }
         }
      } catch (Exception ex) {
        Logger.warn ("could not create "+ filter);
        ex.printStackTrace();
      }
    }
    Class[] r = (Class[]) classNames.toArray(new Class[classNames.size()]);
    return (Class<Object>[])r;
  } 


  protected Class<Object>[] scanInjectableClasses() { 
    if (scannedPackage == null)
      return selectClasses(Helper.getClasses("controllers", app.classloader()));
    else
      return selectClasses(Helper.getClasses(scannedPackage, app.classloader()));
  }
  
  protected String[] moduleNames() {
    if (moduleNames == null) {
      String[] module = new String[1];
      module[0] = "module.Dependencies";
      return module;
    } else  
      return moduleNames.split(",");
  }
  
  private ArrayList<Object> createModules() {
    ArrayList<Object> modules = new ArrayList<Object>();
    try {
      for (String module : moduleNames()) {
         modules.add(Class.forName(module).newInstance());
      }
    } catch (ClassNotFoundException x) {
      Logger.warn("maybe inject.modules config parameter is not set propery?");
      x.printStackTrace();
    } catch (InstantiationException x) {
         Logger.warn("Modules:"+ app.configuration().getString("inject.modules")+" could not be ceated" );
        x.printStackTrace();
    } catch (IllegalAccessException x) {
        x.printStackTrace();
    }
    return modules;
  }

}
