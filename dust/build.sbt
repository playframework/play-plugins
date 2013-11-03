import sbt.Defaults._

sbtPlugin := true

name := "play-plugins-dust"

version := "1.5"

organization := "com.typesafe"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies <++= (scalaVersion) {
  case (scalaVersion) => Seq(
    sbtPluginExtra("com.typesafe.play" % "sbt-plugin" % "2.2.1", "0.13", "2.10")
  )
}

libraryDependencies += "commons-io" % "commons-io" % "2.2"

libraryDependencies += "org.specs2" %% "specs2" % "1.12.3" % "test"

publishMavenStyle := false

publishTo <<= (version) { version: String =>
  val typesafeIvyReleases = Resolver.url("Typesafe Ivy Releases Repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)
  val typesafeIvySnapshot = Resolver.url("Typesafe Ivy Snapshots Repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns)
  val repo =  if (version.trim.endsWith("SNAPSHOT")) typesafeIvySnapshot
                      else typesafeIvyReleases
  Some(repo)
}
