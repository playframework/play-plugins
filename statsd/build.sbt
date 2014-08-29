name := "play-statsd"
    
organization := "com.typesafe.play.plugins"

version := "2.3.0"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.0" % "provided",
  "com.typesafe.play"  %% "play-test" % "2.3.0" % "test"
)

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

publishTo <<= (version) { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/") 
  else                                   Some("releases"  at nexus + "maven-releases/")
}
 
javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-encoding", "UTF-8")

scalacOptions += "-deprecation"
  
lazy val root = project in file(".")

lazy val sample = (project in file("sample/sample-statsd"))
  .enablePlugins(PlayScala)
  .settings(
    Keys.fork in Test := false
  ).dependsOn(root).aggregate(root)
