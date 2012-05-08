import sbt.Defaults._

sbtPlugin := true

name := "play-plugins-sbtgoodies"

version := "0.1"

organization := "com.typesafe"

libraryDependencies <++= (scalaVersion, sbtVersion) { 
	case (scalaVersion, sbtVersion) => Seq(
		sbtPluginExtra("play" % "sbt-plugin" % "2.0.1", sbtVersion, scalaVersion)
	)
}

libraryDependencies += "com.sun.jna" % "jna" % "3.0.9"

publishMavenStyle := false

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

publishTo <<= (version) { version: String =>
  val typesafeIvyReleases = Resolver.url("Typesafe Ivy Releases Repository", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns) 
  val typesafeIvySnapshot = Resolver.url("Typesafe Ivy Snapshots Repository", url("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns) 
  val repo =  if (version.trim.endsWith("SNAPSHOT")) typesafeIvySnapshot
                      else typesafeIvyReleases
  Some(repo)
}
