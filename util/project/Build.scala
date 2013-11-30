import sbt._
import Keys._

object MinimalBuild extends Build {
  

  lazy val buildVersion =  "2.2.1"
  lazy val playVersion = "2.2.1"
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val repo = if (buildVersion.endsWith("SNAPSHOT")) typesafeSnapshot else typesafe  
  
  lazy val play =  "com.typesafe.play" %% "play" % playVersion % "provided"


  lazy val root = Project(id = "play-plugins-util", base = file("."), settings = Project.defaultSettings).settings(
    version := buildVersion,
    scalaVersion := "2.10.2",
    publishTo <<= (version) { version: String =>
                val nexus = "https://private-repo.typesafe.com/typesafe/"
                if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
                else                                   Some("releases"  at nexus + "maven-releases/")
    },
    organization := "com.typesafe",
    resolvers += repo,
    javacOptions += "-Xlint:unchecked",
    libraryDependencies += play
  )
}
