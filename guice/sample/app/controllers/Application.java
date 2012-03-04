package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import javax.inject.*;
import service.*;

public class Application extends Controller {
  
  @Inject static Service s;

  public static Result index() {
    return ok(index.render(s.demonstrate()));
  }
  
}
