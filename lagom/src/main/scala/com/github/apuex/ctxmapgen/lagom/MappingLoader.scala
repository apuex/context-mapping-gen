package com.github.apuex.ctxmapgen.lagom

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import _root_.scala.xml.parsing._
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
      .reduceLeft((l, r) => s"${l}\n${r}")
  }
}

class MappingLoader(val xml: Node) {
  val mapping: String = "mapping"
  val application: String = "app"
  val modelName = xml.\@("name")
  val modelPackage = xml.\@("package")
  val modelVersion = xml.\@("version")
  val system = xml.\@("to")
  val outputDir = s"${System.getProperty("output.dir", "target/generated")}"
  val rootProjectName = s"${cToShell(modelName)}"
  val rootProjectDir = s"${outputDir}/${rootProjectName}"
  val mappingProjectName = s"${cToShell(modelName)}-${cToShell(mapping)}"
  val mappingProjectDir = s"${rootProjectDir}/${mappingProjectName}"
  val srcPackage = s"${modelPackage}.${cToCamel(modelName)}"
  val srcDir = s"${mappingProjectDir}/src/main/scala/${srcPackage.replace('.', '/')}"
  val appProjectName = s"${cToShell(modelName)}-${cToShell(application)}"
  val appProjectDir = s"${rootProjectDir}/${appProjectName}"
  val applicationConfDir = s"${appProjectDir}/conf"
  val symboConverter = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}")
    "new IdentityConverter()" else "new CamelToCConverter()"
  val docsDir = s"${rootProjectDir}/docs"
  val classNamePostfix = s"${cToPascal(mapping)}"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"
}
