import scala.Some

name := "play-plugins-redis"
    
organization := "com.typesafe.play.plugins"

version := "2.3.0"

scalaVersion := "2.10.4"

//scalaVersion := "2.11.1"

//crossScalaVersions := Seq("2.11.1", "2.10.4")

libraryDependencies ++= Seq(
  "com.typesafe.play"         %% "play"               % "2.3.0"     % "provided",
  "biz.source_code"           %  "base64coder"        % "2010-12-19",
  "com.typesafe.play"         %% "play-cache"         % "2.3.0",
  "com.typesafe.play.plugins" %% "play-plugins-util"  % "2.3.0",
  "org.sedis"                 %  "sedis_2.10.0"       % "1.1.1"
)

resolvers ++= Seq(
  "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

publishTo <<= (version) { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
  else                                   Some("releases"  at nexus + "maven-releases/")
}
 
javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-encoding", "UTF-8")

scalacOptions += "-deprecation"
