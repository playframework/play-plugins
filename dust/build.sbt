import sbt.Defaults._

sbtPlugin := true

name := "play-plugins-dust"

version := "1.4.1-09122012"

organization := "com.typesafe"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies <++= (scalaVersion) { 
	case (scalaVersion) => Seq(
		sbtPluginExtra("play" % "sbt-plugin" % "2.1-09092012" % "provided", "0.12", scalaVersion)
	)
}

libraryDependencies += "commons-io" % "commons-io" % "2.2"

publishMavenStyle := false

publishTo <<= (version) { version: String =>
  val typesafeIvyReleases = Resolver.url("Typesafe Ivy Releases Repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns) 
  val typesafeIvySnapshot = Resolver.url("Typesafe Ivy Snapshots Repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns) 
  val repo =  if (version.trim.endsWith("SNAPSHOT")) typesafeIvySnapshot
                      else typesafeIvyReleases
  Some(repo)
}
