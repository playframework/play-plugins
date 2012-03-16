import sbt.Defaults._

sbtPlugin := true

name := "play-plugins-dust"

version := "1.0-SNAPSHOT"

organization := "com.typesafe"

libraryDependencies <++= (scalaVersion, sbtVersion) { 
	case (scalaVersion, sbtVersion) => Seq(
		sbtPluginExtra("play" % "sbt-plugin" % "2.0", sbtVersion, scalaVersion)
	)
}