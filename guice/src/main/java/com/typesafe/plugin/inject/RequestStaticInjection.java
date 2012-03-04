package com.typesafe.plugin.inject;

import com.google.inject.*;
import java.util.*;

/**
 * requests static injection for injectable classes
 */
public class RequestStaticInjection implements Module {
 
  private Class<Object>[] injectables = null;

  public RequestStaticInjection (Class<Object>[] injectables) {
    this.injectables = injectables;
  }

 public void configure(Binder binder) {
    for (Class<Object> o : injectables) {
      binder.requestStaticInjection(o);
    }
  }  
}