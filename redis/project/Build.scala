import sbt._
import Keys._

object MinimalBuild extends Build {
  
  lazy val buildVersion =  "2.0-RC3"
  
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val repo = if (buildVersion.endsWith("SNAPSHOT")) typesafeSnapshot else typesafe  
  
  lazy val play =  "play" %% "play" % buildVersion


  lazy val root = Project(id = "play-plugins-redis", base = file("."), settings = Project.defaultSettings).settings(
    version := buildVersion,
    publishTo <<= (version) { version: String =>
                val nexus = "http://repo.typesafe.com/typesafe/"
                if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "ivy-snapshots/") 
                else                                   Some("releases"  at nexus + "ivy-releases/")
    },
    organization := "com.typesafe",
    resolvers += repo,
    javacOptions += "-Xlint:unchecked",
    libraryDependencies += play,
    crossPaths := false,
    libraryDependencies +=  "org.ow2.spec.ee" % "ow2-atinject-1.0-spec" % "1.0.10",
    libraryDependencies +=  "com.cedarsoft" % "guice-annotations" % "2.0.1"
  )
}
