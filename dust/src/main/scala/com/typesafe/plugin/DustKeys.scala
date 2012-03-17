package com.typesafe.plugin

import sbt._

trait DustKeys {
  lazy val dustFileReplaceRegexp = SettingKey[String]("play-dust-file-regex-from")
  lazy val dustFileReplaceWith = SettingKey[String]("play-dust-file-regex-to")
  lazy val dustAssetsGlob = SettingKey[PathFinder]("play-dust-assets-glob")
  lazy val dustAssetsDir = SettingKey[File]("play-dust-assets-dir")
  lazy val dustFileEnding = SettingKey[String]("play-dust-file-ending")
}