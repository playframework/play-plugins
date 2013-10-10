package com.typesafe.plugin

import org.apache.commons.mail._

import java.util.concurrent.Future
import java.lang.reflect._
import javax.mail.internet.InternetAddress

import scala.collection.JavaConversions._

import play.api._
import play.api.Configuration._

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
   */
  def send(bodyText: String): Unit
  
  /**
   * Sends an email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   * @param bodyHtml : pass a string or use a Play! text template to generate the template
   * like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   */
  def send(bodyText: String, bodyHtml: String): Unit

  /**
   * Sends an Html email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def sendHtml(bodyHtml: String): Unit 
  
}

trait MailerBuilder extends MailerAPI {

  protected val context = new ThreadLocal[collection.mutable.Map[String,List[String]]] {
    protected override def initialValue(): collection.mutable.Map[String,List[String]] = {
      collection.mutable.Map[String,List[String]]()
    }
  }

  /**
   * extract parameter key from context
   * @param key
   */
  protected def e(key: String): List[String] = {
	val splitIndex = key.indexOf("-");
    if (splitIndex >= 0)
      context.get.toList
		  .filter(_._1 startsWith key.substring(0, splitIndex)) //get the keys that have the parameter key
		  .map(e=> e._1.substring(splitIndex+1)+":"+e._2.head) //column cannot be part of a header's name, so we can use this for splitting.
    else
      context.get.get(key).getOrElse(List[String]())
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
   * Sends a text email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def send(bodyText: String): Unit = send(bodyText, "")

    /**
   * Sends an Html email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def sendHtml(bodyHtml: String): Unit = send("", bodyHtml)

}

/**
 * providers an Emailer using apache commons-email
 * (the implementation si based on
 *  the EmailNotifier trait by Aishwarya Singhal
 *  and also Justin Long's gist)
 */

class CommonsMailer(smtpHost: String,smtpPort: Int,smtpSsl: Boolean, smtpTls: Boolean, smtpUser: Option[String], smtpPass: Option[String]) extends MailerBuilder {

  /**
   * Sends an email based on the provided data. 
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   * @param bodyHtml : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def send(bodyText: String, bodyHtml: String): Unit = {
    val email = createEmailer(bodyText,bodyHtml)
    email.setCharset(e("charset").headOption.getOrElse("utf-8"))
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
    email.setHostName(smtpHost)
    email.setSmtpPort(smtpPort)
    email.setSSL(smtpSsl)
    email.setTLS(smtpTls)
    for(u <- smtpUser; p <- smtpPass) yield email.setAuthenticator(new DefaultAuthenticator(u, p))
    email.setDebug(false)
    email.send
    context.get.clear()
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
        val iAddress = new InternetAddress(emailAddress);
        val address = iAddress.getAddress()
        val name = iAddress.getPersonal()

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
  private def createEmailer(bodyText: String, bodyHtml: String): MultiPartEmail = {
    if (bodyHtml == null || bodyHtml == "") {
      val e = new MultiPartEmail()
      e.setMsg(bodyText)
      e
    } else if (bodyText == null || bodyText == "")
        new HtmlEmail().setHtmlMsg(bodyHtml)
      else
        new HtmlEmail().setHtmlMsg(bodyHtml).setTextMsg(bodyText)
  }

}

/**
 * Emailer that just prints out the content to the console
 */

case object MockMailer extends MailerBuilder {

  def send(bodyText: String, bodyHtml: String): Unit = {
    Logger.info("MOCK MAILER: send email")
    e("from").foreach(from => Logger.info("FROM:" + from))
    e("replyTo").foreach(replyTo => Logger.info("REPLYTO:" + replyTo))
    e("recipients").foreach(to => Logger.info("TO:" + to))
    e("ccRecipients").foreach(cc => Logger.info("CC:" + cc))
    e("bccRecipients").foreach(bcc => Logger.info("BCC:" + bcc))
	e("header-") foreach(header => Logger.info("HEADER:" + header))
    if (bodyText != null && bodyText != "") {

      Logger.info("TEXT: " + bodyText)
    }
    if (bodyHtml != null && bodyHtml != "") {
      Logger.info("HTML: " + bodyHtml)
    }
    context.get.clear()
  }

}

/**
 * plugin interface
 */
trait MailerPlugin extends  play.api.Plugin {
  def email: MailerAPI
}

/**
 * plugin impelementation
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
    new CommonsMailer(smtpHost, smtpPort, smtpSsl, smtpTls, smtpUser, smtpPassword)
  }

  override lazy val enabled = {
    !app.configuration.getString("apachecommonsmailerplugin").filter(_ == "disabled").isDefined
  }

  override def onStart() {
    mailerInstance
  }

  def email = mailerInstance
}

