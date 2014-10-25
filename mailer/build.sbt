name := "play-plugins-mailer"
    
organization := "com.typesafe.play.plugins"

version := "2.3.1"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.1" % "provided",
  "org.apache.commons" % "commons-email" % "1.3.3",
  "com.typesafe.play.plugins" %% "play-plugins-util" % "2.3.0",
  "org.specs2" %% "specs2-core" % "2.4.9" % "test"
)

publishTo <<= (version) { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
  else                                   Some("releases"  at nexus + "maven-releases/")
}
 
javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-encoding", "UTF-8")

scalacOptions += "-deprecation"
