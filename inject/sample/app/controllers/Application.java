package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import javax.inject.*;
import service.*;

public class Application extends Controller {
  
  @Inject private static MyService s;

  // used for testing, not used by Play
  //public Application(MyService s){
  //  this.s = s;
  //}

  public static Result index() {
    return ok(index.render(s.demonstrate()));
  }
  
}