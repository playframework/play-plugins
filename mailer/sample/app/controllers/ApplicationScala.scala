package controllers

import java.io.File

import com.typesafe.plugin.{MailerPlugin, MailerAPI}
import org.apache.commons.mail.EmailAttachment
import play.api.mvc.{Action, Controller}
import play.api.Play.current

/**
 * @author bloemgracht
 */
object ApplicationScala  extends Controller {

  def index = Action {
    val mail: MailerAPI = play.Play.application.plugin(classOf[MailerPlugin]).email
    mail.setSubject("simplest mailer test")
    mail.setRecipient("some display name <sometoadd@email.com>")
    mail.setFrom("some display name <somefromadd@email.com>")
    mail.addAttachment("favicon.png", new File(current.classloader.getResource("public/images/favicon.png").getPath))
    val data: Array[Byte] = "data".getBytes
    mail.addAttachment("data.txt", data, "text/plain", "A simple file", EmailAttachment.INLINE)
    mail.send("A text only message", "<html><body><p>An <b>html</b> message</p></body></html>")
    Ok(views.html.index("Your new application is ready."))
  }
}
