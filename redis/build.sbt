name := "play-modules-redis"

organization := "com.typesafe.play.modules"

version := "2.4.0"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.11.2", "2.10.4")

libraryDependencies ++= Seq(
  "com.typesafe.play"         %% "play"               % "2.4.0"     % "provided",
  "com.typesafe.play"         %% "play-cache"         % "2.4.0",
  "biz.source_code"           %  "base64coder"        % "2010-12-19",
  "org.sedis"                 %% "sedis"              % "1.2.2",
  "com.typesafe.play"         %% "play-test"          % "2.4.0"     % "test",
  "com.typesafe.play"         %% "play-specs2"        % "2.4.0"     % "test",
  "org.specs2"                %% "specs2-core"        % "3.3.1"     % "test"
)

resolvers ++= Seq(
  "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

publishTo <<= (version) { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/")
  else                                   Some("releases"  at nexus + "maven-releases/")
}

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-encoding", "UTF-8")

scalacOptions += "-deprecation"
