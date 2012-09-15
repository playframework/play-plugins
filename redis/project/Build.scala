import sbt._
import Keys._

object MinimalBuild extends Build {
  
  lazy val buildVersion =  "2.1-09092012"
  
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val repo = if (buildVersion.endsWith("SNAPSHOT")) typesafeSnapshot else typesafe  
  
  lazy val root = Project(id = "play-plugins-redis", base = file("."), settings = Project.defaultSettings).settings(
    version := buildVersion,
    publishTo <<= (version) { version: String =>
                val nexus = "http://typesafe.artifactoryonline.com/typesafe/"
                if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
                else                                   Some("releases"  at nexus + "maven-releases/")
    },
    organization := "com.typesafe",
    resolvers += repo,
    javacOptions += "-Xlint:unchecked",
    libraryDependencies += "biz.source_code" % "base64coder" % "2010-09-21",
    libraryDependencies += "com.typesafe" %% "play-plugins-util" % buildVersion,
    libraryDependencies += "org.sedis" % "sedis_2.9.2" % "1.1.0"
  )
}
