package com.typesafe.plugin

import java.io.File
import javax.mail.Part

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
  }

  class MockMultiPartEmail extends MultiPartEmail {
    override def getPrimaryBodyPart = super.getPrimaryBodyPart
    override def getContainer = super.getContainer
  }
  class MockHtmlEmail extends HtmlEmail {
    def getHtml = this.html
    def getText = this.text
    override def getPrimaryBodyPart = super.getPrimaryBodyPart
    override def getContainer = super.getContainer
  }
  object MockCommonsMailer extends CommonsMailer("typesafe.org", 1234, true, false, Some("user"), Some("password"), false) {
    override def send(email: MultiPartEmail) = ""
    override def createEmail(bodyText: String, bodyHtml: String) = super.createEmail(bodyText, bodyHtml)
    override def createMultiPartEmail(): MultiPartEmail = new MockMultiPartEmail
    override def createHtmlEmail(): HtmlEmail = new MockHtmlEmail
    def getAttachmentContext = attachmentContext.get()
    def getContext = context.get()
  }

  "The CommonsMailer" should {
    "configure SMTP" in {
      val mail = MockCommonsMailer
      val email = mail.createEmail("A text only message", "")
      email.getSmtpPort mustEqual "1234"
      email.getSslSmtpPort mustEqual "1234"
      email.getMailSession.getProperty("mail.smtp.auth") mustEqual "true"
      email.getMailSession.getProperty("mail.smtp.host") mustEqual "typesafe.org"
      email.getMailSession.getProperty("mail.smtp.starttls.enable") mustEqual "false"
      email.getMailSession.getProperty("mail.debug") mustEqual "false"
    }

    "create an empty email" in {
      val mail = MockCommonsMailer
      mail.setSubject("Subject")
      mail.setRecipient("Guillaume Grossetie <ggrossetie@localhost.com>")
      mail.setFrom("James Roper <jroper@typesafe.com>")
      val messageId = mail.send("")
      messageId mustEqual ""
    }

    "create a simple email" in {
      val mail = createSimpleEmail
      val email = mail.createEmail("A text message", "<html><body><p>An <b>html</b> message</p></body></html>")
      simpleEmailMust(email)
      email must beAnInstanceOf[HtmlEmail]
      email must beAnInstanceOf[MockHtmlEmail]
      email.asInstanceOf[MockHtmlEmail].getText mustEqual "A text message"
      email.asInstanceOf[MockHtmlEmail].getHtml mustEqual "<html><body><p>An <b>html</b> message</p></body></html>"
    }

    "create a simple email with attachment" in {
      val mail = createSimpleEmail
      mail.addAttachment("play icon", getPlayIcon)
      val email = mail.createEmail("Text message", "")
      simpleEmailMust(email)
      email must beAnInstanceOf[MultiPartEmail]
      email must beAnInstanceOf[MockMultiPartEmail]
      email.asInstanceOf[MockMultiPartEmail].getContainer.getCount mustEqual 2
      val textPart = email.asInstanceOf[MockMultiPartEmail].getContainer.getBodyPart(0)
      textPart.getContentType mustEqual "text/plain"
      textPart.getContent mustEqual "Text message"
      email.asInstanceOf[MockMultiPartEmail].getPrimaryBodyPart.getContent mustEqual "Text message"
      val attachmentPart = email.asInstanceOf[MockMultiPartEmail].getContainer.getBodyPart(1)
      attachmentPart.getFileName mustEqual "play icon"
      attachmentPart.getDescription mustEqual "play icon"
      attachmentPart.getDisposition mustEqual Part.ATTACHMENT
    }

    "create a simple email with inline attachment and description" in {
      val mail = createSimpleEmail
      mail.addAttachment("play icon", getPlayIcon, "A beautiful icon", Part.INLINE)
      val email = mail.createEmail("Text message", "")
      simpleEmailMust(email)
      email must beAnInstanceOf[MultiPartEmail]
      email must beAnInstanceOf[MockMultiPartEmail]
      email.asInstanceOf[MockMultiPartEmail].getContainer.getCount mustEqual 2
      val textPart = email.asInstanceOf[MockMultiPartEmail].getContainer.getBodyPart(0)
      textPart.getContentType mustEqual "text/plain"
      textPart.getContent mustEqual "Text message"
      email.asInstanceOf[MockMultiPartEmail].getPrimaryBodyPart.getContent mustEqual "Text message"
      val attachmentPart = email.asInstanceOf[MockMultiPartEmail].getContainer.getBodyPart(1)
      attachmentPart.getFileName mustEqual "play icon"
      attachmentPart.getDescription mustEqual "A beautiful icon"
      attachmentPart.getDisposition mustEqual Part.INLINE
    }
  }
  def simpleEmailMust(email: MultiPartEmail) {
    email.getSubject mustEqual "Subject"
    email.getFromAddress.getPersonal mustEqual "James Roper"
    email.getFromAddress.getAddress mustEqual "jroper@typesafe.com"
    email.getToAddresses must have size 1
    email.getToAddresses.get(0).getPersonal mustEqual "Guillaume Grossetie"
    email.getToAddresses.get(0).getAddress mustEqual "ggrossetie@localhost.com"
  }

  def getPlayIcon: File = {
    new File(Thread.currentThread().getContextClassLoader.getResource("play_icon_full_color.png").toURI)
  }

  def createSimpleEmail = {
    val mail = MockCommonsMailer
    mail.setSubject("Subject")
    mail.setFrom("James Roper <jroper@typesafe.com>")
    mail.setRecipient("Guillaume Grossetie <ggrossetie@localhost.com>")
    mail
  }
}
