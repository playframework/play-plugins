package com.typesafe.plugin

package notifiers

import org.apache.commons.mail._

import java.util.concurrent.Future
import java.lang.reflect._
import javax.mail.internet.InternetAddress

import scala.collection.JavaConversions._

import play.api._
import play.api.Configuration._

trait MailerAPI {
  /**
   * Sets a subject for this email. It enables formatting of the providing string using Java's
   * string formatter.
   *
   * @param subject
   * @param args
   */
  def setSubject(subject: String, args: AnyRef*): MailerAPI 
  
  /**
   * Defines the sender of this email("from" address).
   *
   * @param from
   */
  def addFrom(from: String): MailerAPI 

  /**
   * Adds an email recipient in CC.
   *
   * @param ccRecipients
   */
  def addCc(ccRecipients: String*): MailerAPI

  /**
   * Adds an email recipient in BCC.
   *
   * @param bccRecipients
   */
  def addBcc(bccRecipients: String*): MailerAPI 
  /**
   * Adds an email recipient ("to" addressee).
   *
   * @param recipients
   */
  def addRecipient(recipients: String*): MailerAPI 
  
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
   * Sets the content type for the email. If none is set, by default it is assumed to be "UTF-8".
   * @param contentType
   */
  def setContentType(contentType: String): MailerAPI 
  
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
}

/**
 * providers an Emailer using apache commons-email
 * (the implementation si based on 
 *  the EmailNotifier trait by Aishwarya Singhal 
 *  and also Justin Long's gist)
 */

class CommonsMailer(smtpHost: String,smtpPort: Int,smtpSsl: Boolean, smtpUser: Option[String], smtpPass: Option[String]) extends MailerAPI{

  private val context = collection.mutable.Map[String,List[String]]()

  /**
   * Sets a subject for this email. It enables formatting of the providing string using Java's
   * string formatter.
   *
   * @param subject
   * @param args
   */
  def setSubject(subject: String, args: AnyRef*): MailerAPI = {
    context += ("subject" -> List(String.format(subject, args: _*)))
    this
  }
  
  /**
   * Defines the sender of this email("from" address).
   *
   * @param from
   */
  def addFrom(from: String): MailerAPI = {
    context += ("from"-> List(from))
    this
  }

  /**
   * Adds an email recipient in CC.
   *
   * @param ccRecipients
   */
  def addCc(ccRecipients: String*): MailerAPI = {
    context += ("ccRecipients"->ccRecipients.toList)
    this
  }

  /**
   * Adds an email recipient in BCC.
   *
   * @param bccRecipients
   */
  def addBcc(bccRecipients: String*): MailerAPI = {
    context += ("bccRecipients"->bccRecipients.toList)
    this
  }
  
  /**
   * Adds an email recipient ("to" addressee).
   *
   * @param recipients
   */
  def addRecipient(recipients: String*): MailerAPI = {
    context += ("recipients"->recipients.toList)
    this
  }
  
  /**
   * Defines the "reply to" email address.
   *
   * @param replyTo
   */
  def setReplyTo(replyTo: String): MailerAPI = {
    context += ("replyTo"->List(replyTo))
    this
  }
  
  /**
   * Sets the charset for this email.
   *
   * @param charset
   */
  def setCharset(charset: String): MailerAPI = {
     context += ("charset"->List(charset))
     this
  }

  /**
   * Sets the content type for the email. If none is set, by default it is assumed to be "UTF-8".
   * @param contentType
   */
  def setContentType(contentType: String): MailerAPI = {
    context += ("contentType"->List(contentType))
    this
  }
  
  /**
   * Adds a request header to this email message.
   *
   * @param key
   * @param value
   */
  def addHeader(key: String, value: String): MailerAPI  = {
    context += ("header-"+key->List(value))
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
  def send(bodyText: String): Unit = send(bodyText,"")
  
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
    e("header-") foreach (e => email.addHeader(e.split("-")(0), e.split("-")(1)))
    email.setHostName(smtpHost)
    email.setSmtpPort(smtpPort)
    email.setSSL(smtpSsl)
    for(u <- smtpUser; p <- smtpPass) yield email.setAuthenticator(new DefaultAuthenticator(u, p))
    email.setDebug(false)
    email.send

  }
  
  /**
   * extract parameter key from context
   * @param key 
   */
  private def e(key: String): List[String] = {
    if (key.contains("-"))
      context.toList.filter(_._1 == key.split("-")(0)).map(e=> e._1.split("-")(1)+"-"+e._2.head)
    else
      context.get(key).getOrElse(List[String]())
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
    val contentType = e("contentType").headOption.getOrElse("text/plain")
    if (contentType == "text/plain" && bodyHtml == "") {
      val e = new MultiPartEmail()
      e.setMsg(bodyText)
      e
    } else 
      new HtmlEmail().setHtmlMsg(bodyHtml).setTextMsg(bodyText)
    
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
  
  private lazy val smtpHost = app.configuration.getString("smtp.host").getOrElse(throw new RuntimeException("smtp.host needs to be set in appliction.conf in order to use this plugin"))
  private lazy val smtpPort = app.configuration.getInt("smtp.port").getOrElse(25)
  private lazy val smtpSsl = app.configuration.getBoolean("smtp.ssl").getOrElse(false)
  private lazy val smtpUser = app.configuration.getString("smtp.user")
  private lazy val smtpPassword = app.configuration.getString("smtp.password")

  private lazy val mailerInstance = new CommonsMailer(smtpHost,smtpPort,smtpSsl, smtpUser, smtpPassword)

  override lazy val enabled = {
    !app.configuration.getString("apachecommonsmailerplugin").filter(_ == "disabled").isDefined
  }

  override def onStart() {
    mailerInstance
  }

  def email = mailerInstance
}

