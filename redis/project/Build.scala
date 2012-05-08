import sbt._
import Keys._

object MinimalBuild extends Build {
  
  lazy val buildVersion =  "2.0.1"
  
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val repo = if (buildVersion.endsWith("SNAPSHOT")) typesafeSnapshot else typesafe  
  
  lazy val root = Project(id = "play-plugins-redis", base = file("."), settings = Project.defaultSettings).settings(
    version := buildVersion,
    publishTo <<= (version) { version: String =>
                val nexus = "http://repo.typesafe.com/typesafe/"
                if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "ivy-snapshots/") 
                else                                   Some("releases"  at nexus + "ivy-releases/")
    },
    organization := "com.typesafe",
    resolvers += repo,
    resolvers += "Sedis" at "http://guice-maven.googlecode.com/svn/trunk",
    javacOptions += "-Xlint:unchecked",
    libraryDependencies += "play" %% "play" % buildVersion,
    libraryDependencies += "biz.source_code" % "base64coder" % "2010-09-21",
    libraryDependencies += "com.typesafe" %% "play-plugins-util" % buildVersion,
    libraryDependencies += "org.sedis" %% "sedis" % "1.0.1"
  )
}
