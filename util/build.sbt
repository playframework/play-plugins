name := "play-plugins-util"
    
organization := "com.typesafe.play.plugins"

version := "2.3.1"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.1" % "provided"
)

publishTo <<= (version) { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
  else                                   Some("releases"  at nexus + "maven-releases/")
}
 
javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked")

scalacOptions += "-deprecation"
