import play.Play.autoImport._
import PlayKeys._
import sbt._
import Keys._

object ApplicationBuild extends Build {

    val appName         = "play-plugin-redis-sample-app"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
       "com.typesafe" %% "play-plugins-redis" % "2.3.0",
       "com.typesafe.play" %% "play-cache" % "2.3.0"
    )

    val main = Project(appName, file(".")).enablePlugins(play.PlayJava).settings(
      version := appVersion,
      scalaVersion := "2.11.1",
      libraryDependencies ++= appDependencies
    )

}
