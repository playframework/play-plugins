package com.typesafe.plugin

import sbt._
import Keys._

object SbtGoodiesPlugin extends Plugin with SbtGoodiesTasks {

  val distUnzipSettings: Seq[Setting[_]] = Seq(
     distUnzip <<= distUnzipTask,
     distUnzip <<= distUnzip.dependsOn(PlayProject.dist)
  )
}
