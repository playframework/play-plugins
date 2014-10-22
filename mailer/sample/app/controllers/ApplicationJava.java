package controllers;

import java.io.File;

import org.apache.commons.mail.EmailAttachment;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class ApplicationJava extends Controller {
  
  public static Result index() {
    MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
    mail.setSubject("simplest mailer test");
    mail.setRecipient("some display name <sometoadd@email.com>");
    mail.setFrom("some display name <somefromadd@email.com>");
    mail.addAttachment("favicon.png", new File(Play.application().classloader().getResource("public/images/favicon.png").getPath()));
    byte[] data = "data".getBytes();
    mail.addAttachment("data.txt", data, "text/plain", "A simple file", EmailAttachment.INLINE);
    mail.send("A text only message", "<html><body><p>An <b>html</b> message</p></body></html>" );

    return ok(index.render("Your new application is ready."));
  }
  
}
