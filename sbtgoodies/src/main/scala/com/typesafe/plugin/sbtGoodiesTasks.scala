package com.typesafe.plugin


import sbt._
import PlayProject._
import Keys._
import NameFilter._
import com.sun.jna.Library
import com.sun.jna.Native

trait CLibrary extends Library {
  def chmod(path: String, mode: Int): Int
}

trait SbtGoodiesTasks extends SbtGoodiesKeys {

  lazy val distUnzipTask = (distDirectory, baseDirectory, version, normalizedName, streams) map { (distDir, baseDir, version, id,s) =>
    val packageName = id + "-" + version
    val zip = distDir / (packageName + ".zip")
    val unzippedDir = distDir / packageName
    if (zip.exists) {
      IO.unzip(zip, distDir)
      val os = System.getProperty("os.name").toLowerCase()

      if (os.indexOf("win") <= 0) {
        //nix
        val libc = Native.loadLibrary("c", classOf[CLibrary]).asInstanceOf[CLibrary]
        libc.chmod(unzippedDir.getAbsolutePath+"/start", 0755)
      } else {
        //win
         val config = Option(System.getProperty("config.file"))
         val startbat = distDir / "start.bat"
         IO.write(startbat,
          """|@echo off
             |setlocal
             |set p=%~dp0
             |set p=%p:\=/%
             |java %* -cp "%p%lib/*" """ + config.map(fn => "-Dconfig.file=\"%p%" + fn + "\" ").getOrElse("") + """play.core.server.NettyServer "%p%"
             |""".stripMargin)
      }
      IO.delete(zip)
      s.log.info("Done!")
      s.log.info("Your unzipped distribution is ready in ")
      s.log.info(unzippedDir.getAbsolutePath)
    } else s.log.warn("could not find dist archive:"+zip.getAbsolutePath)
  }
}
