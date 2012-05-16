package com.typesafe.util

import sbt._

object Sbt {
  def logger(extra: ScopedKey[_] => Seq[AbstractLogger]) = new TypesafeLogManager(extra)
}

  class TypesafeLogManager(extra: ScopedKey[_] => Seq[AbstractLogger]) extends LogManager {
  
  val screen = LogManager.defaultScreen
  val backed = LogManager.defaultBacked()
  
  def apply(data: Settings[Scope], state: State, task: ScopedKey[_], to: java.io.PrintWriter): Logger = {
    new FilterLogger(
      delegate = LogManager.defaultLogger(data, state, task, screen, backed(to), extra(task).toList).asInstanceOf[AbstractLogger]
    ) {

      override def log(level: Level.Value, message: => String) {
        if(atLevel(level)) {
          if(filtered(message)) {
            print(".")
          } else {
            if (message.toLowerCase.contains("update") || message.toLowerCase.contains("updating")) println()
            super.log(level, message)
          }
        }
      }

      def filtered(message: String) = {
        message.startsWith("Resolving ") && message.endsWith("...")
      }

    }
   }
}
