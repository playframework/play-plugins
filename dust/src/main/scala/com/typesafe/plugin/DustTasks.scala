package com.typesafe.plugin

import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import org.apache.commons.io.FilenameUtils
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
      new InputStreamReader(this.getClass.getClassLoader.getResource("dust-full-0.6.0.js").openConnection().getInputStream()),
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
        
        // dust.js has weird error reporting where the line/column are part of the message, so we have to use a Regex to find them
        val DustCompileError = ".* At line : (\\d+), column : (\\d+)".r
        
        message match {
          case DustCompileError(line, column) => Left(message, line.toInt, column.toInt)
          case _ => Left(message, 0, 0) // Some other weird error, we have no line/column info now.
        }
      }
    }
  }

  import Keys._

  lazy val DustCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, dustFileRegexFrom, dustFileRegexTo, dustAssetsDir, dustAssetsGlob) map {
    (src, resources, cache, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
      val cacheFile = cache / "dust"

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != currentInfos) {

        previousGeneratedFiles.foreach(IO.delete)

        val generated = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val msg = compile(FilenameUtils.removeExtension(sourceFile.getPath.replace(assetsDir.getPath + "/", "")), IO.read(sourceFile)).left.map {
              case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                msg,
                line,
                column)
            }.right.get

            val out = new File(resources, "public/" + naming(name))
            IO.write(out, msg)
            Seq(sourceFile -> out)
          }
        }

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ generated,
          currentInfos)(FileInfo.lastModified.format)

        generated.map(_._2).distinct.toList
      } else {
        previousGeneratedFiles.toSeq
      }
  }

}