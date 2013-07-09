import org.specs2.mutable._
import org.specs2.specification._

import com.typesafe.plugin._
import java.io.File

class DustTasksSpec extends Specification with DustTasks {
  trait testData extends Scope {
    val output = """(function(){dust.register("test",body_0);function body_0(chk,ctx){return chk.write("Hello ").reference(ctx.get("name"),ctx,"h").write("! You have ").reference(ctx.get("count"),ctx,"h").write(" new messages.");}return body_0;})();"""
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
    "use embedded compiler by default" in new testData {
      val msg = compile("test", new java.io.File(getClass.getResource("/test.tl").toURI))
      msg must beRight
      msg.right.get === output
    }
    "defer to native compiler if one is specified and located" in new testData {
      val msg = compile("test", new java.io.File(getClass.getResource("/test.tl").toURI), 
        Some("/usr/local/share/npm/bin/dustc"))  
      msg must beRight
      msg.right.get === output
    }
    "revert to embedded compiler if native one is specified but not located" in new testData {
      val msg = compile("test", new java.io.File(getClass.getResource("/test.tl").toURI), 
        Some("a/road/to/nowhere/"))  
      msg must beRight
      msg.right.get === output
    }
  }
}