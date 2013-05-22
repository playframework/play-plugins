import sbt.Defaults._

sbtPlugin := true

name := "play-plugins-dust"

version := "1.5"

organization := "com.typesafe"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions ++= Seq("-deprecation")

addSbtPlugin("play" % "sbt-plugin" % "2.1.1")

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.specs2" %% "specs2" % "1.12.3" % "test"

publishMavenStyle := false

publishTo <<= (version) { version: String =>
  val typesafeIvyReleases = Resolver.url("Typesafe Ivy Releases Repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns) 
  val typesafeIvySnapshot = Resolver.url("Typesafe Ivy Snapshots Repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns) 
  val repo =  if (version.trim.endsWith("SNAPSHOT")) typesafeIvySnapshot
                      else typesafeIvyReleases
  Some(repo)
}
