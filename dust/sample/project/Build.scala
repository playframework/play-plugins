import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
    val appName         = "play-plugins-dust-sample"
    val appVersion      = "1.5"
 

    val appDependencies = Seq()

    val main = play.Project(appName, appVersion, appDependencies).settings()
}
