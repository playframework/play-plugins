package com.typesafe.plugin

import java.io.File

import org.apache.commons.mail._

import java.io.FilterOutputStream
import java.io.PrintStream
import javax.mail.internet.InternetAddress

import play.api._

trait MailerAPI extends MailerApiJavaInterop {

  /* Sets a subject for this email.*/
  def setSubject(subject: String): MailerAPI

  /**
   * Defines the sender of this email("from" address).
   *
   * @param from
   */
  def setFrom(from: String): MailerAPI

  /**
   * Defines the "reply to" email address.
   *
   * @param replyTo
   */
  def setReplyTo(replyTo: String): MailerAPI

  /**
   * Sets the charset for this email.
   *
   * @param charset
   */
  def setCharset(charset: String): MailerAPI

  /**
   * Adds a request header to this email message.
   *
   * @param key
   * @param value
   */
  def addHeader(key: String, value: String): MailerAPI

   /**
   * Sends a text email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   * @return the message id
   */
  def send(bodyText: String): String

  /**
   * Sends an email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   * @param bodyHtml : pass a string or use a Play! text template to generate the template
   * like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return the message id
   */
  def send(bodyText: String, bodyHtml: String): String

  /**
   * Sends an Html email based on the provided data. 
   *
   * @param bodyHtml : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return the message id
   */
  def sendHtml(bodyHtml: String): String

}

trait MailerBuilder extends MailerAPI {

  protected val context = new ThreadLocal[collection.mutable.Map[String,List[String]]] {
    protected override def initialValue(): collection.mutable.Map[String,List[String]] = {
      collection.mutable.Map[String,List[String]]()
    }
  }

  case class Attachment(data: Option[Array[Byte]],
                        mimetype: Option[String],
                        filePath: Option[String],
                        name: String,
                        description: Option[String],
                        disposition: Option[String])

  protected val attachmentContext = new ThreadLocal[collection.mutable.MutableList[Attachment]] {
    protected override def initialValue() = {
      collection.mutable.MutableList()
    }
  }

  /**
   * extract parameter key from context
   * @param key
   */
  protected def e(key: String): List[String] = {
    val splitIndex = key.indexOf("-")
    if (splitIndex >= 0)
      context.get.toList
        .filter(_._1 startsWith key.substring(0, splitIndex)) //get the keys that have the parameter key
        .map(e => e._1.substring(splitIndex + 1) + ":" + e._2.head) //column cannot be part of a header's name, so we can use this for splitting.
    else
      context.get.getOrElse(key, List[String]())
  }

  /**
   * Sets a subject for this email. It enables formatting of the providing string using Java's
   * string formatter.
   *
   * @param subject
   * @param args
   */
  def setSubject(subject: String, args: AnyRef*): MailerAPI = {
    context.get += ("subject" -> List(String.format(subject, args: _*)))
    this
  }

  def setSubject(subject: String): MailerAPI = {
    context.get += ("subject" -> List(subject))
    this
  }
  /**
   * Defines the sender of this email("from" address).
   *
   * @param from
   */
  def setFrom(from: String): MailerAPI = {
    context.get += ("from"-> List(from))
    this
  }

  /**
   * Adds an email recipient in CC.
   *
   * @param ccRecipients
   */
  def setCc(ccRecipients: String*): MailerAPI = {
    context.get += ("ccRecipients"->ccRecipients.toList)
    this
  }

  /**
   * Adds an email recipient in BCC.
   *
   * @param bccRecipients
   */
  def setBcc(bccRecipients: String*): MailerAPI = {
    context.get += ("bccRecipients"->bccRecipients.toList)
    this
  }
  
  /**
   * Adds an email recipient ("to" addressee).
   *
   * @param recipients
   */
  def setRecipient(recipients: String*): MailerAPI = {
    context.get += ("recipients"->recipients.toList)
    this
  }
  
  /**
   * Defines the "reply to" email address.
   *
   * @param replyTo
   */
  def setReplyTo(replyTo: String): MailerAPI = {
    context.get += ("replyTo"->List(replyTo))
    this
  }
  
  /**
   * Sets the charset for this email.
   *
   * @param charset
   */
  def setCharset(charset: String): MailerAPI = {
     context.get += ("charset"->List(charset))
     this
  }

  
  
  /**
   * Adds a request header to this email message.
   *
   * @param key
   * @param value
   */
  def addHeader(key: String, value: String): MailerAPI  = {
    context.get += ("header-"+key->List(value))
    this
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param data
   * @param mimetype
   */
  def addAttachment(name: String, data: Array[Byte], mimetype: String): MailerAPI = {
    addAttachment(name, data, mimetype, None, None)
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param data
   * @param mimetype
   * @param description
   */
  def addAttachment(name: String, data: Array[Byte], mimetype: String, description: String): MailerAPI = {
    addAttachment(name, data, mimetype, description, null)
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param data
   * @param mimetype
   * @param description
   * @param disposition
   */
  def addAttachment(name: String, data: Array[Byte], mimetype: String, description: String, disposition: String): MailerAPI = {
    addAttachment(name, data, mimetype, if (description != null) Some(description) else None, if (disposition != null) Some(disposition) else None)
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param file
   */
  def addAttachment(name: String, file: File): MailerAPI = {
    addAttachment(name, file, null)
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param file
   * @param description
   */
  def addAttachment(name: String, file: File, description: String): MailerAPI = {
    addAttachment(name, file, description, null)
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param file
   * @param description
   * @param disposition
   */
  def addAttachment(name: String, file: File, description: String, disposition: String): MailerAPI = {
    addAttachment(name, file, if (description != null) Some(description) else None, if (disposition != null) Some(disposition) else None)
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param data
   * @param mimetype
   * @param description
   * @param disposition
   */
  private def addAttachment(name: String, data: Array[Byte], mimetype: String, description: Option[String], disposition: Option[String]): MailerAPI = {
    attachmentContext.get += Attachment(Some(data), Some(mimetype), None, name, description, disposition)
    this
  }

  /**
   * Adds attachment to this email message.
   *
   * @param name
   * @param file
   * @param description
   * @param disposition
   */
  private def addAttachment(name: String, file: File, description: Option[String], disposition: Option[String]): MailerAPI  = {
    attachmentContext.get += Attachment(None, None, Some(file.getPath), name, description, disposition)
    this
  }

  /**
   * Sends a text email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def send(bodyText: String): String = send(bodyText, "")

    /**
   * Sends an Html email based on the provided data. 
   *
   * @param bodyHtml : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def sendHtml(bodyHtml: String): String = send("", bodyHtml)

}

/**
 * providers an Emailer using apache commons-email
 * (the implementation si based on
 *  the EmailNotifier trait by Aishwarya Singhal
 *  and also Justin Long's gist)
 */
abstract class CommonsMailer(smtpHost: String, smtpPort: Int, smtpSsl: Boolean, smtpTls: Boolean, smtpUser: Option[String], smtpPass: Option[String], debugMode: Boolean) extends MailerBuilder {

  def send(email: MultiPartEmail): String

  def createMultiPartEmail(): MultiPartEmail

  def createHtmlEmail(): HtmlEmail

  /**
   * Sends an email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   * @param bodyHtml : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def send(bodyText: String, bodyHtml: String): String = {
    val email = createEmail(bodyText, bodyHtml)
    val messageId = send(email)
    context.get.clear()
    attachmentContext.get.clear()
    messageId
  }

  protected def createEmail(bodyText: String, bodyHtml: String):MultiPartEmail = {
    val email = createEmailBody(bodyText,bodyHtml,e("charset").headOption.getOrElse("utf-8"))
    email.setSubject(e("subject").headOption.getOrElse(""))
    e("from").foreach(setAddress(_) { (address, name) => email.setFrom(address, name) })
    e("replyTo").foreach(setAddress(_) { (address, name) => email.addReplyTo(address, name) })
    e("recipients").foreach(setAddress(_) { (address, name) => email.addTo(address, name) })
    e("ccRecipients").foreach(setAddress(_) { (address, name) => email.addCc(address, name) })
    e("bccRecipients").foreach(setAddress(_) { (address, name) => email.addBcc(address, name) })
    e("header-") foreach (e => {
      val split = e.indexOf(":")
      email.addHeader(e.substring(0,split), e.substring(split+1))
    })
    attachmentContext.get.foreach { case attachment =>
      val description = attachment.description.getOrElse(attachment.name)
      val disposition = attachment.disposition.getOrElse(EmailAttachment.ATTACHMENT)
      for {
        data <- attachment.data
        mimetype <- attachment.mimetype
      } yield {
        val dataSource = new javax.mail.util.ByteArrayDataSource(data, mimetype)
        email.attach(dataSource, attachment.name, description, disposition)
      }
      for {
        path <- attachment.filePath
      } yield {
        val emailAttachment = new EmailAttachment()
        emailAttachment.setName(attachment.name)
        emailAttachment.setPath(path)
        emailAttachment.setDescription(description)
        emailAttachment.setDisposition(disposition)
        email.attach(emailAttachment)
      }
    }
    email.setHostName(smtpHost)
    email.setSmtpPort(smtpPort)
    email.setSSLOnConnect(smtpSsl)
    if (smtpSsl) {
      email.setSslSmtpPort(smtpPort.toString)
    }
    email.setStartTLSEnabled(smtpTls)
    for(u <- smtpUser; p <- smtpPass) yield email.setAuthenticator(new DefaultAuthenticator(u, p))
    if (debugMode && Logger.isDebugEnabled) {
      email.setDebug(debugMode)
      email.getMailSession.setDebugOut(new PrintStream(new FilterOutputStream(null) {
        override def write(b: Array[Byte]) {
          Logger.debug(new String(b))
        }

        override def write(b: Array[Byte], off: Int, len: Int) {
          Logger.debug(new String(b, off, len))
        }

        override def write(b: Int) {
          this.write(new Array(b):Array[Byte])
        }
      }))
    }
    email
  }

  /**
   * Extracts an email address from the given string and passes to the enclosed method.
   *
   * @param emailAddress
   * @param setter
   */
  private def setAddress(emailAddress: String)(setter: (String, String) => Unit) = {

    if (emailAddress != null) {
      try {
        val iAddress = new InternetAddress(emailAddress)
        val address = iAddress.getAddress
        val name = iAddress.getPersonal

        setter(address, name)
      } catch {
        case e: Exception =>
          setter(emailAddress, null)
      }
    }
  }

  /**
   * Creates an appropriate email object based on the content type.
   *
   * @param bodyText
   * @param bodyHtml
   * @return
   */
  private def createEmailBody(bodyText: String, bodyHtml: String, charset: String): MultiPartEmail = {
    val bodyHtmlOpt = Option(bodyHtml).filter(_.trim.nonEmpty)
    val bodyTextOpt = Option(bodyText).filter(_.trim.nonEmpty)
    if (bodyHtmlOpt.isDefined) {
      // HTML...
      val e = createHtmlEmail()
      e.setCharset(charset)
      e.setHtmlMsg(bodyHtmlOpt.get)
      // ... with text ?
      if (bodyTextOpt.isDefined) {
        e.setTextMsg(bodyTextOpt.get)
      }
      e
    } else if (bodyTextOpt.isDefined) {
      // Text only
      val e = createMultiPartEmail()
      e.setCharset(charset)
      e.setMsg(bodyText)
      e
    } else {
      // Both empty
      createMultiPartEmail()
    }
  }
}

/**
 * Emailer that just prints out the content to the console
 */
case object MockMailer extends MailerBuilder {

  def send(bodyText: String, bodyHtml: String): String = {
    Logger.info("MOCK MAILER: send email")
    e("subject").foreach(subject => Logger.info("SUBJECT:" + subject))
    e("from").foreach(from => Logger.info("FROM:" + from))
    e("replyTo").foreach(replyTo => Logger.info("REPLYTO:" + replyTo))
    e("recipients").foreach(to => Logger.info("TO:" + to))
    e("ccRecipients").foreach(cc => Logger.info("CC:" + cc))
    e("bccRecipients").foreach(bcc => Logger.info("BCC:" + bcc))
    e("header-") foreach (header => Logger.info("HEADER:" + header))
    attachmentContext.get foreach (attachment => Logger.info("ATTACHMENT:" + attachment))
    if (bodyText != null && bodyText != "") {
      Logger.info("TEXT: " + bodyText)
    }
    if (bodyHtml != null && bodyHtml != "") {
      Logger.info("HTML: " + bodyHtml)
    }
    context.get.clear()
    attachmentContext.get.clear()
    null
  }
}

/**
 * plugin interface
 */
trait MailerPlugin extends  play.api.Plugin {
  def email: MailerAPI
}

/**
 * plugin implementation
 */
class CommonsMailerPlugin(app: play.api.Application) extends MailerPlugin {

  private lazy val mock = app.configuration.getBoolean("smtp.mock").getOrElse(false)

  private lazy val mailerInstance: MailerAPI = if (mock) {
    MockMailer
  } else {
    val smtpHost = app.configuration.getString("smtp.host").getOrElse(throw new RuntimeException("smtp.host needs to be set in application.conf in order to use this plugin (or set smtp.mock to true)"))
    val smtpPort = app.configuration.getInt("smtp.port").getOrElse(25)
    val smtpSsl = app.configuration.getBoolean("smtp.ssl").getOrElse(false)
    val smtpTls = app.configuration.getBoolean("smtp.tls").getOrElse(false)
    val smtpUser = app.configuration.getString("smtp.user")
    val smtpPassword = app.configuration.getString("smtp.password")
    val debugMode = app.configuration.getBoolean("smtp.debug").getOrElse(false)
    new CommonsMailer(smtpHost, smtpPort, smtpSsl, smtpTls, smtpUser, smtpPassword, debugMode) {
      override def send(email: MultiPartEmail) = email.send()
      override def createMultiPartEmail() = new MultiPartEmail()
      override def createHtmlEmail() = new HtmlEmail()
    }
  }

  override lazy val enabled = {
    !app.configuration.getString("apachecommonsmailerplugin").filter(_ == "disabled").isDefined
  }

  override def onStart() {
    mailerInstance
  }

  def email = mailerInstance
}

