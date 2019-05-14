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
  val api: String = "api"
  val impl: String = "impl"
  val mapping: String = "mapping"
  val app: String = "app"
  val modelName = xml.\@("name")
  val modelPackage = xml.\@("package")
  val modelVersion = xml.\@("version")
  val modelMaintainer = xml.\@("maintainer")
  val system = xml.\@("to")
  val outputDir = s"${System.getProperty("output.dir", "target/generated")}"
  val rootProjectName = s"${cToShell(modelName)}"
  val rootProjectDir = s"${outputDir}/${rootProjectName}"
  val apiProjectName = s"${cToShell(modelName)}-${cToShell(api)}"
  val apiProjectDir = s"${rootProjectDir}/${api}"
  val implProjectName = s"${cToShell(modelName)}-${cToShell(impl)}"
  val implProjectDir = s"${rootProjectDir}/${impl}"
  val apiSrcPackage = s"${modelPackage}.${cToCamel(modelName)}"
  val apiSrcDir = s"${apiProjectDir}/src/main/scala/${apiSrcPackage.replace('.', '/')}"
  val implSrcPackage = s"${apiSrcPackage}.${cToCamel(impl)}"
  val implSrcDir = s"${implProjectDir}/src/main/scala/${implSrcPackage.replace('.', '/')}"
  val appProjectName = s"${cToShell(modelName)}-${cToShell(app)}"
  val appProjectDir = s"${rootProjectDir}/${app}"
  val applicationConfDir = s"${appProjectDir}/conf"
  val symboConverter = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}")
    "new IdentityConverter()" else "new CamelToCConverter()"
  val docsDir = s"${rootProjectDir}/docs"
  val classNamePostfix = s"${cToPascal(impl)}"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"
}
