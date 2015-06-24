name := "play-modules-redis"
organization := "com.typesafe.play.modules"

scalaVersion := "2.11.6"
crossScalaVersions := Seq("2.11.6", "2.10.5")
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-encoding", "UTF-8")
scalacOptions += "-deprecation"

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
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

pomExtra := {
  <scm>
    <url>https://github.com/typesafehub/play-plugins</url>
    <connection>scm:git:git@github.com:typesafehub/play-plugins.git</connection>
  </scm>
  <developers>
    <developer>
      <id>typesafe</id>
      <name>Typesafe</name>
      <url>https://typesafe.com</url>
    </developer>
  </developers>
}
pomIncludeRepository := { _ => false }
homepage := Some(url(s"https://github.com/typesafehub/play-ulgins"))
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

sonatypeProfileName := "com.typesafe"
releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseTagName := s"redis-${(version in ThisBuild).value}"
releaseCrossBuild := true

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  releaseStepCommand("sonatypeRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

