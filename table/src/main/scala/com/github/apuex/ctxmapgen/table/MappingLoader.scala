package com.github.apuex.ctxmapgen.table

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml.parsing._
import scala.xml._

object MappingLoader {
  def apply(fileName: String): MappingLoader = {
    val factory = new NoBindingFactoryAdapter
    new MappingLoader(factory.load(fileName))
  }

  def apply(xml: Node): MappingLoader = new MappingLoader(xml)

  def importPackagesForService(model: Node, service: Node): String = {
    s"""
       |${importPackages(service)}
       |${importPackages(model)}
     """.stripMargin
      .trim
  }

  def importPackages(node: Node): String = {
    node.child.filter(x => x.label == "imports")
      .flatMap(x => x.child.filter(c => c.label == "import"))
      .map(x => x.text.trim)
      .map(x => x.replace("*", "_"))
      .map(x => x.replace("static", ""))
      .map(x => s"import ${x}")
      .foldLeft("")((l, r) => s"${l}\n${r}")
      .trim
  }
}

class MappingLoader(val xml: Node) {
  val mapping = "mapping"
  val api = "api"
  val impl = "impl"
  val srcSystem = xml.\@("from")
  val destSystem = xml.\@("to")
  val modelName = if("" == xml.\@("name").trim) s"${srcSystem}_${destSystem}_${mapping}" else xml.\@("name")
  val modelPackage = xml.\@("package")
  val modelVersion = xml.\@("version")
  val modelMaintainer = xml.\@("maintainer")
  val outputDir = s"${System.getProperty("output.dir", "target/generated")}"
  val rootProjectName = s"${cToShell(modelName)}"
  val rootProjectDir = s"${outputDir}/${rootProjectName}"
  val apiProjectName = s"${cToShell(modelName)}-${api}"
  val apiProjectDir = s"${rootProjectDir}/${api}"
  val apiSrcPackage = s"${modelPackage}"
  val apiSrcDir = s"${apiProjectDir}/src/main/scala/${modelPackage.replace('.', '/')}"
  val implProjectName = s"${cToShell(modelName)}-${impl}"
  val implProjectDir = s"${rootProjectDir}/${impl}"
  val implSrcPackage = s"${modelPackage}.${impl}"
  val implSrcDir = s"${implProjectDir}/src/main/scala/${implSrcPackage.replace('.', '/')}"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"
}
