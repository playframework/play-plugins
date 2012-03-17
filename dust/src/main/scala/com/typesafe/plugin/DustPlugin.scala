package com.typesafe.plugin

import sbt._
import Keys._

object DustPlugin extends Plugin with DustTasks {

  override def settings: Seq[Setting[_]] = super.settings ++ Seq(
    dustAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets")),
    dustFileEnding := ".tl",
    dustAssetsGlob <<= (dustAssetsDir)(assetsDir => assetsDir ** "*.tl"),
    dustFileReplaceRegexp <<= (dustFileEnding)(fileEnding => "*" + fileEnding),
    dustFileReplaceWith <<= (dustFileEnding)(fileEnding => "*" + fileEnding + ".js"),
    resourceGenerators in Compile <+= DustCompiler)

}