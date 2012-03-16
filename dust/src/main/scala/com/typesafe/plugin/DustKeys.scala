package com.typesafe.plugin

import sbt._

trait DustKeys {
  lazy val dustEntryPoints = SettingKey[PathFinder]("play-dust-entry-points")
  lazy val dustOptions = SettingKey[Seq[String]]("play-dust-options")
}