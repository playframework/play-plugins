package com.typesafe.plugin

import sbt._

trait SbtGoodiesKeys {
    val distUnzip = TaskKey[Unit]("dist-unzip", "unzip the standalone application package")
}