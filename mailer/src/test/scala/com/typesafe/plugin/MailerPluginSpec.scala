package com.typesafe.plugin

import java.io.File

import org.apache.commons.mail.{HtmlEmail, MultiPartEmail}
import org.specs2.mutable._

class MailerPluginSpec extends Specification {

  "The MailerAPI" should {
    "fill internal contexts" in {
      object MockMailer extends MailerBuilder {
        override def send(bodyText: String, bodyHtml: String) = ""
        def getAttachmentContext = attachmentContext.get()
        def getContext = context.get()
      }
      val mail = MockMailer
      mail.setSubject("simplest mailer test")
      mail.setRecipient("some display name <sometoadd@email.com>")
      mail.setFrom("some display name <somefromadd@email.com>")
      mail.addAttachment("some", new File("some.png"))
      mail.send("A text message", "")
      val attachments = mail.getAttachmentContext
      attachments must have size 1
      attachments.head.name mustEqual "some"
      attachments.head.data must beNone setMessage "No data defined"
      attachments.head.description must beNone setMessage "No description defined"
      attachments.head.disposition must beNone setMessage "No disposition defined"
      attachments.head.mimetype must beNone setMessage "No mimetype defined"
      attachments.head.filePath must beSome("some.png")
    }

    "configure SMTP" in {
      object MockMailer extends CommonsMailer("typesafe.org", 1234, true, false, Some("user"), Some("password"), false) {
        override def send(email: MultiPartEmail) = ""
        override def createEmail(bodyText: String, bodyHtml: String) = super.createEmail(bodyText, bodyHtml)
      }
      val mail = MockMailer
      val email = mail.createEmail("A text only message", "")
      email.getSmtpPort mustEqual "1234"
      email.getSslSmtpPort mustEqual "1234"
      email.getMailSession.getProperty("mail.smtp.auth") mustEqual "true"
      email.getMailSession.getProperty("mail.smtp.host") mustEqual "typesafe.org"
      email.getMailSession.getProperty("mail.smtp.starttls.enable") mustEqual "false"
      email.getMailSession.getProperty("mail.debug") mustEqual "false"
    }

    "create en email" in {
      object MockMailer extends CommonsMailer("", 1234, false, false, None, None, true) {
        override def send(email: MultiPartEmail) = ""
        override def createEmail(bodyText: String, bodyHtml: String) = super.createEmail(bodyText, bodyHtml)
      }
      val mail = MockMailer
      mail.setSubject("Subject")
      mail.setFrom("James Roper <jroper@typesafe.com>")
      mail.setRecipient("Guillaume Grossetie <ggrossetie@localhost.com>")
      val email = mail.createEmail("A text message", "<html><body><p>An <b>html</b> message</p></body></html>")
      email.getSubject mustEqual "Subject"
      email.getFromAddress.getPersonal mustEqual "James Roper"
      email.getFromAddress.getAddress mustEqual "jroper@typesafe.com"
      email.getToAddresses must have size 1
      email.getToAddresses.get(0).getPersonal mustEqual "Guillaume Grossetie"
      email.getToAddresses.get(0).getAddress mustEqual "ggrossetie@localhost.com"
    }

    "create en email with attachment" in {
      object MockMailer extends CommonsMailer("", 1234, false, false, None, None, true) {
        override def send(email: MultiPartEmail) = ""
        override def createEmail(bodyText: String, bodyHtml: String) = super.createEmail(bodyText, bodyHtml)
      }
      val mail = MockMailer
      mail.setSubject("Subject")
      mail.setFrom("James Roper <jroper@typesafe.com>")
      mail.setRecipient("Guillaume Grossetie <ggrossetie@localhost.com>")
      val playIconFile = new File(Thread.currentThread().getContextClassLoader.getResource("play_icon_full_color.png").toURI)
      mail.addAttachment("play icon", playIconFile)
      val email = mail.createEmail("Text message", "")
      email.getSubject mustEqual "Subject"
      email.getFromAddress.getPersonal mustEqual "James Roper"
      email.getFromAddress.getAddress mustEqual "jroper@typesafe.com"
      email.getToAddresses must have size 1
      email.getToAddresses.get(0).getPersonal mustEqual "Guillaume Grossetie"
      email.getToAddresses.get(0).getAddress mustEqual "ggrossetie@localhost.com"
      email must beAnInstanceOf[MultiPartEmail]
    }
  }
}
