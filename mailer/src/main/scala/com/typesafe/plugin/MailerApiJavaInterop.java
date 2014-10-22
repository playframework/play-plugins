package com.typesafe.plugin;

import java.io.File;

public interface MailerApiJavaInterop {

  /**
   * Sets a subject for this email. It enables formatting of the providing string using Java's
   * string formatter.
   *
   * @param subject
   * @param args
   */
  public MailerAPI setSubject(String subject, java.lang.Object... args);

  /**
   * Adds an email recipient in CC.
   *
   * @param ccRecipients
   */
  public MailerAPI setCc(String... ccRecipients);

  /**
   * Adds an email recipient in BCC.
   *
   * @param bccRecipients
   */
  public MailerAPI setBcc(String... bccRecipients);

  /**
   * Adds an email recipient ("to" addressee).
   *
   * @param recipients
   */
  public  MailerAPI setRecipient(String... recipients);

  /**
   * Adds an attachment to this email message.
   *
   * @param name The name of the attachment, will also be used as the description (if not explicitly defined)
   * @param data A byte array of the contents of the attachment
   * @param mimetype The mimetype of the attachment
   */
  public MailerAPI addAttachment(String name, byte[] data, String mimetype);

  /**
   * Adds an attachment to this email message.
   *
   * @param name The name of the attachment, will also be used as the description (if not explicitly defined)
   * @param data A byte array of the contents of the attachment
   * @param mimetype The mimetype of the attachment
   * @param description The description of the attachment, by default the name of the attachment
   */
  public MailerAPI addAttachment(String name, byte[] data, String mimetype, String description);

  /**
   * Adds an attachment to this email message.
   *
   * @param name The name of the attachment, will also be used as the description (if not explicitly defined)
   * @param data A byte array of the contents of the attachment
   * @param mimetype The mimetype of the attachment
   * @param description The description of the attachment, by default the name of the attachment
   * @param disposition The disposition of the attachment, by default {@code javax.mail.Part.ATTACHMENT}
   */
  public MailerAPI addAttachment(String name, byte[] data, String mimetype, String description, String disposition);

  /**
   * Adds an attachment to this email message.
   *
   * @param name The name of the attachment, will also be used as the description (if not explicitly defined)
   * @param file The file to add as an attachment
   */
  public MailerAPI addAttachment(String name, File file);

  /**
   * Adds an attachment to this email message.
   *
   * @param name The name of the attachment, will also be used as the description (if not explicitly defined)
   * @param file The file to add as an attachment
   * @param description The description of the attachment, by default the name of the attachment
   */
  public MailerAPI addAttachment(String name, File file, String description);

  /**
   * Adds an attachment to this email message.
   *
   * @param name The name of the attachment, will also be used as the description (if not explicitly defined)
   * @param file The file to add as an attachment
   * @param description The description of the attachment, by default the name of the attachment
   * @param disposition The disposition of the attachment, by default {@code javax.mail.Part.ATTACHMENT}
   */
  public MailerAPI addAttachment(String name, File file, String description, String disposition);
}