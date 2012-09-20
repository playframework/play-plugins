import sbt.Defaults._

sbtPlugin := true

name := "play-plugins-dust"

version := "1.4.1"

organization := "com.typesafe"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies <++= (scalaVersion, sbtVersion) { 
	case (scalaVersion, sbtVersion) => Seq(
		sbtPluginExtra("play" % "sbt-plugin" % "2.0.2", sbtVersion, scalaVersion)
	)
}

libraryDependencies += "commons-io" % "commons-io" % "2.2"

publishMavenStyle := false

publishTo <<= (version) { version: String =>
  val typesafeIvyReleases = Resolver.url("Typesafe Ivy Releases Repository", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns) 
  val typesafeIvySnapshot = Resolver.url("Typesafe Ivy Snapshots Repository", url("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns) 
  val repo =  if (version.trim.endsWith("SNAPSHOT")) typesafeIvySnapshot
                      else typesafeIvyReleases
  Some(repo)
}
