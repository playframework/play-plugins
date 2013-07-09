package com.typesafe.plugin

import java.io.{ File, FileInputStream, InputStreamReader }
import org.apache.commons.io.FilenameUtils
import org.mozilla.javascript.tools.shell.Global
import org.mozilla.javascript.{ Context, JavaScriptException, Scriptable, ScriptableObject}

import sbt._
import sbt.PlayExceptions.AssetCompilationException

trait DustTasks extends DustKeys {
  def handleCompileError(message: String) = {
    // dust.js has weird error reporting where the line/column are part of the message, so we have to use a Regex to find them
    val DustCompileError = ".* At line : (\\d+), column : (\\d+)".r
    
    message match {
      case DustCompileError(line, column) => Left(message, line.toInt, column.toInt)
      case _ => Left(message, 0, 0) // Some other weird error, we have no line/column info now.
    }
  }
  
  def compile(name: String, source: java.io.File, nativePath: Option[String] = None): Either[(String, Int, Int), String] = {
    nativePath match {
      case Some(nativeCompiler) if (new java.io.File(nativeCompiler).exists) =>
        compileNative(name, source.getPath(), nativeCompiler).left.map {
          case (msg, line, column) => throw AssetCompilationException(Some(source),
            msg,
            Some(line),
            Some(column))
        }
      case _ =>
        compileEmbedded(name, IO.read(source)).left.map {
          case (msg, line, column) => throw AssetCompilationException(Some(source),
            msg,
            Some(line),
            Some(column))
        }
    }
  }
  
  def compileEmbedded(name: String, source: String): Either[(String, Int, Int), String] = {
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
      new InputStreamReader(this.getClass.getClassLoader.getResource("dust-full-1.2.3.js").openConnection().getInputStream()),
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
        
        handleCompileError(message)
      }
    }
  }
  
  def compileNative(name: String, sourceFile: String, nativePath: String): Either[(String, Int, Int), String] = {
    import scala.sys.process._
    val pb = Process(nativePath + " --name=" + name + " " + sourceFile)
    var out = List[String]()
    var err = List[String]()
    val exit = pb ! ProcessLogger((s) => out ::= s, (s) => err ::= s)
    if (exit != 0) {
      val message = err.mkString("");
     
      handleCompileError(message)
    }
    else {
      Right(out.mkString(""))
    }
  }

  protected def templateName(sourceFile: String, assetsDir: String): String = {
    val sourceFileWithForwardSlashes = FilenameUtils.separatorsToUnix(sourceFile)
    val assetsDirWithForwardSlashes  = FilenameUtils.separatorsToUnix(assetsDir)
    FilenameUtils.removeExtension(
      sourceFileWithForwardSlashes.replace(assetsDirWithForwardSlashes + "/", "")
    )
  }
  
  import Keys._

  lazy val DustCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, dustFileRegexFrom, dustFileRegexTo, dustAssetsDir, dustAssetsGlob, dustOutputRelativePath, dustNativePath) map {
    (src, resources, cache, fileReplaceRegexp, fileReplaceWith, assetsDir, files, outputRelativePath, nativePath) =>
      
      val cacheFile = cache / "dust"

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s
      
      if (previousInfo != currentInfos) {

        previousGeneratedFiles.foreach(IO.delete)

        val generated = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val msg = compile(templateName(sourceFile.getPath, assetsDir.getPath), sourceFile, nativePath)
            val out = new File(resources, "public/" + outputRelativePath + naming(name))
            IO.write(out, msg.right.get)
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
