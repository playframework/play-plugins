import sbt._
import Keys._

object MinimalBuild extends Build {
  
  lazy val buildVersion =  "2.2.1"
  
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val repo = if (buildVersion.endsWith("SNAPSHOT")) typesafeSnapshot else typesafe  
  lazy val pk11 = "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk"
  // for sedis 1.1.9 which hasn't been merged to master and doesn't exist in any repo yet
  lazy val mavenLocal = "Maven Local" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
  lazy val root = Project(id = "play-plugins-redis", base = file("."), settings = Project.defaultSettings).settings(
    version := "2.2.1",
    scalaVersion := "2.10.2",
    publishTo <<= (version) { version: String =>
                val nexus = "https://private-repo.typesafe.com/typesafe/"
                if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
                else                                   Some("releases"  at nexus + "maven-releases/")
    },
    organization := "com.typesafe",
    resolvers += repo,
    resolvers += pk11,
    resolvers += mavenLocal,
    javacOptions += "-Xlint:unchecked",
    libraryDependencies += "biz.source_code" % "base64coder" % "2010-12-19",
    libraryDependencies += "com.typesafe" %% "play-plugins-util" % buildVersion,
    libraryDependencies += "com.typesafe.play" %% "play-cache" % buildVersion % "provided",
    libraryDependencies += "org.sedis" % "sedis_2.10.0" % "1.1.9"
  )
}
