package com.typesafe.plugin

import sbt._
import Keys._

object SbtGoodiesPlugin extends Plugin with SbtGoodiesTasks {

  override def settings: Seq[Setting[_]] = super.settings ++ Seq(
     distUnzip <<= distUnzipTask,
     distUnzip <<= distUnzip.dependsOn(PlayProject.dist)
  )
}
