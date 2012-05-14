package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import javax.inject.*;
import service.*;

public class Application extends Controller {

  private MyService s;

  @Inject public Application( MyService s) {
    this.s=s;
  }
  
  public  Result index() {
    return ok(index.render(s.demonstrate()));
  }
  
}
