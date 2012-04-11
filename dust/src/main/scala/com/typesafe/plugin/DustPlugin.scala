package com.typesafe.plugin

import sbt._
import Keys._
import org.apache.commons.io.FilenameUtils

object DustPlugin extends Plugin with DustTasks {

  override def settings: Seq[Setting[_]] = super.settings ++ Seq(
    dustAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets")),
    dustFileEnding := ".tl",
    dustAssetsGlob <<= (dustAssetsDir)(assetsDir => assetsDir ** "*.tl"),
    dustFileRegexFrom <<= (dustFileEnding)(fileEnding => fileEnding),
    dustFileRegexTo <<= (dustFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),
    resourceGenerators in Compile <+= DustCompiler)

}