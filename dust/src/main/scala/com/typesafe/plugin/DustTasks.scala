package com.typesafe.plugin

import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import org.mozilla.javascript.tools.shell.Global
import org.mozilla.javascript.Context
import org.mozilla.javascript.JavaScriptException
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

import sbt._
import PlayProject._

trait DustTasks extends DustKeys {
  def compile(name: String, source: String): Either[(String, Int, Int), String] = {

    import org.mozilla.javascript._
    import org.mozilla.javascript.tools.shell._

    import com.typesafe.plugin.DustKeys;

    import scala.collection.JavaConversions._

    import java.io._

    val ctx = Context.enter
    val global = new Global; global.init(ctx)
    val scope = ctx.initStandardObjects(global)

    ctx.evaluateReader(
      scope,
      new InputStreamReader(this.getClass.getClassLoader.getResource("dust-0.3.js").openConnection().getInputStream()),
      "dust.js",
      1, null)

    ScriptableObject.putProperty(scope, "rawSource", source)
    ScriptableObject.putProperty(scope, "name", name)

    try {
      Right(ctx.evaluateString(scope, "(dust.compile(rawSource, name))", "JDustCompiler", 0, null).toString)
    } catch {
      case e: JavaScriptException => {
        val jsError = e.getValue.asInstanceOf[Scriptable]
        val message = ScriptableObject.getProperty(jsError, "message").toString
        val line = ScriptableObject.getProperty(jsError, "fileName").toString.toDouble.toInt
        val column = ScriptableObject.getProperty(jsError, "lineNumber").toString.toDouble.toInt
        Left(message, line, column)
      }
    }
  }

  lazy val DustCompiler = AssetsCompiler("dust",
    (_ ** "*.tpl"),
    dustEntryPoints,
    { (name, min) => name.replace(".tpl", ".tpl.js") },
    {
      (tplFile, options) =>
        compile(tplFile.getName, IO.read(tplFile)).right.map { compiled =>
          (compiled, None, Seq(tplFile))
        }.left.map {
          case (msg, line, column) => throw AssetCompilationException(Some(tplFile),
            msg,
            line,
            column)
        }.right.get
    },
    dustOptions)

}