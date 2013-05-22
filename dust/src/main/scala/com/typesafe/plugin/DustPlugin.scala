package com.typesafe.plugin

import sbt._
import Keys._
import play.Project._
import org.apache.commons.io.FilenameUtils

object DustPlugin extends Plugin with DustTasks {
  override def projectSettings = Seq(
    resourceGenerators in Compile <+= DustCompiler
  )
  
  val defaultSettings = Seq(
    dustAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets")),
    dustOutputRelativePath := "",
    dustNativePath := None,
    dustFileEnding := ".tl",
    dustAssetsGlob <<= (dustAssetsDir)(assetsDir => assetsDir ** "*.tl"),
    dustFileRegexFrom <<= (dustFileEnding)(fileEnding => fileEnding),
    dustFileRegexTo <<= (dustFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js")
  )
}
