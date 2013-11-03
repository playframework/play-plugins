import org.specs2.mutable._
import org.specs2.specification._
import com.typesafe.plugin._
import java.io.File

class HelloWorldSpec extends Specification with DustTasks {

  trait testData extends Scope {
    val validTemplate = """(function(){dust.register("templates/post",body_0);function body_0(chk,ctx){return chk.write("<h1>").reference(ctx.get("title"),ctx,"h",["s"]).write("</h1>").reference(ctx.get("content"),ctx,"h",["s"]);}return body_0;})();"""
  }

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
    "compile a valid template file" in new testData {
      val result = compile("templates/post", io.Source.fromFile(getFile("valid.tl")).mkString)
      result must beRight
      result.right.get == validTemplate
    }
    "correctly handle errors in an invalid template file" in {
      val result = compile("templates/post", io.Source.fromFile(getFile("invalid.tl")).mkString)
      result.left.get.toString must contain("Expected end tag for stream but it was not found.")
    }
  }
  def getFile(fileName: String):File = {
    new File(this.getClass.getClassLoader.getResource(fileName).toURI)
  }
}