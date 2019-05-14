package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.lagom.MappingLoader._

class ApplicationGenerator(mappingLoader: MappingLoader) {
  import mappingLoader._
  def generate(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implSrcDir}/Main.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${implSrcPackage}\n")
    // imports
    printWriter.println(s"${importPackages(xml)}")
    // launcher(main class) object declaration
    printWriter.println(
      s"""
         |object Main extends App {
         |}
       """.stripMargin)
    printWriter.close()
  }
}
