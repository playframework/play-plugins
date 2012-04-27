package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import com.typesafe.plugin.*;

public class Application extends Controller {
  
  public static Result index() {
    MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
    mail.setSubject("simplest mailer test");
    mail.addRecipient("some display name <sometoadd@email.com>");
    mail.addFrom("some dispaly name <somefromadd@email.com>");
    mail.send("A text only message", "<html><body><p>An <b>html</b> message</p></body></html>" );

    return ok(index.render("Your new application is ready."));
  }
  
}
