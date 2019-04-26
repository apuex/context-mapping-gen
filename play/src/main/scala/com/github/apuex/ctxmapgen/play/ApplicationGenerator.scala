package com.github.apuex.ctxmapgen.play

import java.io.{File, PrintWriter}
import MappingLoader._

class ApplicationGenerator(mappingLoader: MappingLoader) {
  import mappingLoader._
  def generate(): Unit = {
    new File(srcDir).mkdirs()
    val printWriter = new PrintWriter(s"${srcDir}/Main.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${srcPackage}\n")
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
