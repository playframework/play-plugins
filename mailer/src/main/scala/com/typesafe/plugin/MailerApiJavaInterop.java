package com.typesafe.plugin;

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
  
}