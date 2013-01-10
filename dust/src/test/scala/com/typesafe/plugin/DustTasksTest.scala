import org.specs2.mutable._
import com.typesafe.plugin._

class HelloWorldSpec extends Specification with DustTasks {
  "The Dust compiler" should {
    "resolve template names correctly on Unix" in {
      val file = "/var/projects/sample/app/assets/templates/foo.tl"
      val assetsDir = "/var/projects/sample/app/assets"
      templateName(file, assetsDir) must be_==("templates/foo")
    }
    "resolve template names correctly on Windows" in {
      val file = "C:\\projects\\sample\\app\\assets\\templates\\foo.tl"
      val assetsDir = "C:\\projects\\sample\\app\\assets"
      templateName(file, assetsDir) must be_==("templates/foo")
    }
  }
}