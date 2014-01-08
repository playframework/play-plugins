import sbt._
import Keys._

object MinimalBuild extends Build {
  
  lazy val buildVersion =  "2.2.0"
  
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val repo = if (buildVersion.endsWith("SNAPSHOT")) typesafeSnapshot else typesafe  
  lazy val pk11 = "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk"
  // for jedis-2.2.2-SNAPSHOT which sedis 1.2.0 depends on
  lazy val sonatype = "sonatype repo" at "http://oss.sonatype.org/content/groups/public"
  lazy val root = Project(id = "play-plugins-redis", base = file("."), settings = Project.defaultSettings).settings(
    version := "2.2.1-SNAPSHOT",
    scalaVersion := "2.10.2",
    //publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath+"/.m2/repository"))),
    publishTo <<= (version) { version: String =>
                val nexus = "https://private-repo.typesafe.com/typesafe/"
                if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
                else                                   Some("releases"  at nexus + "maven-releases/")
    },
    organization := "com.typesafe",
    resolvers += repo,
    resolvers += pk11,
    resolvers += sonatype,
    javacOptions += "-Xlint:unchecked",
    libraryDependencies += "biz.source_code" % "base64coder" % "2010-12-19",
    libraryDependencies += "com.typesafe" %% "play-plugins-util" % buildVersion,
    libraryDependencies += "com.typesafe.play" %% "play-cache" % buildVersion % "provided",
    libraryDependencies += "org.sedis" % "sedis_2.10.0" % "1.2.0"
  )
}
