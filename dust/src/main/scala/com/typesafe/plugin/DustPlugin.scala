package com.typesafe.plugin

import sbt._
import Keys._

object DustPlugin extends Plugin with DustTasks {

  override def settings: Seq[Setting[_]] = super.settings ++ Seq(
    dustOptions := Nil,
    dustEntryPoints <<= (sourceDirectory in Compile)(base => ((base ** "*.tpl"))),
    resourceGenerators in Compile <+= DustCompiler)

}