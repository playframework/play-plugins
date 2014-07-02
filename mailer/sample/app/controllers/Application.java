package controllers;

import org.apache.commons.mail.EmailAttachment;

import play.*;
import play.mvc.*;

import views.html.*;

import com.typesafe.plugin.*;

public class Application extends Controller {
  
  public static Result index() {
    MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
    mail.setSubject("simplest mailer test");
    mail.setRecipient("some display name <sometoadd@email.com>");
    mail.setFrom("some display name <somefromadd@email.com>");
    mail.addAttachment("favicon.png", Play.application().classloader().getResource("public/images/favicon.png").getPath());
    byte[] data = "data".getBytes();
    mail.addAttachment("data.txt", data, "text/plain", "A simple file", EmailAttachment.INLINE);
    mail.send("A text only message", "<html><body><p>An <b>html</b> message</p></body></html>" );

    return ok(index.render("Your new application is ready."));
  }
  
}
