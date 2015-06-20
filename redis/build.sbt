import scala.Some

name := "play-plugins-redis"

organization := "com.typesafe.play.plugins"

version := "2.4.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.play"         %% "play"               % "2.4.0"     % "provided",
  "biz.source_code"           %  "base64coder"        % "2010-12-19",
  "com.typesafe.play"         %% "play-cache"         % "2.4.0",
  "org.sedis"                 %%  "sedis"             % "1.2.2"
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

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-encoding", "UTF-8")

scalacOptions += "-deprecation"
