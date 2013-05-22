package com.typesafe.plugin

import sbt._

trait DustKeys {
  lazy val dustFileRegexFrom = SettingKey[String]("play-dust-file-regex-from")
  lazy val dustFileRegexTo = SettingKey[String]("play-dust-file-regex-to")
  lazy val dustAssetsGlob = SettingKey[PathFinder]("play-dust-assets-glob")
  lazy val dustAssetsDir = SettingKey[File]("play-dust-assets-dir")
  lazy val dustOutputRelativePath = SettingKey[String]("play-dust-output-relative-path")
  lazy val dustNativePath = SettingKey[Option[String]]("play-dust-native-path")
  lazy val dustFileEnding = SettingKey[String]("play-dust-file-ending")
}