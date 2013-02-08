import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "j"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      playJava,
      "com.typesafe" %% "play-plugins-mailer" % "2.1.0"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
    )

}
