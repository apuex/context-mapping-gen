package com.github.apuex.ctxmapgen.table

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.table.TableMappingGenerator._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node


object ServiceClientGenerator {
  def apply(fileName: String): ServiceClientGenerator = new ServiceClientGenerator(MappingLoader(fileName))
  def apply(mappingLoader: MappingLoader): ServiceClientGenerator = new ServiceClientGenerator(mappingLoader)
}

class ServiceClientGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    generateSrcServiceClient()
    generateDestServiceClient()
  }

  def generateSrcServiceClient(): Unit = {

  }

  def generateDestServiceClient(): Unit = {

  }

  def generateRetrieveCalls(): String = {
    val srcTables = xml.filter(x => x.label == "src-table")
    val byRowids = srcTables
      .map(x => {
        val name = x.\@("name")
        s"""
           |def retrieve${cToPascal(name)}ByRowid: ServiceCall[RetrieveByRowidCmd, ${cToPascal(name)}Vo]
         """.stripMargin.trim
      })

    val byKeys = srcTables
      .map(x => x.child.filter(_.label == "view"))
      .flatMap(x => x)
      .map(x => {
        val name = x.\@("name")
        s"""
           |def retrieve${cToPascal(name)}: ServiceCall[Retrieve${cToPascal(name)}Cmd, ${cToPascal(name)}Vo]
         """.stripMargin.trim
      })

    (byRowids ++ byKeys)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def updates(): String = {
    val tables = destTableNames(xml)

    (tables.map(create(_)) ++
      tables.map(update(_)) ++
      tables.map(delete(_)))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def create(name: String): String = {
    s"""
       |def create${cToPascal(name)}: ServiceCall[Create${cToPascal(name)}Cmd, NotUsed]
         """.stripMargin.trim
  }

  def update(name: String): String = {
    s"""
       |def update${cToPascal(name)}: ServiceCall[Update${cToPascal(name)}Cmd, NotUsed]
         """.stripMargin.trim
  }

  def delete(name: String): String = {
    s"""
       |def delete${cToPascal(name)}: ServiceCall[Delete${cToPascal(name)}Cmd, NotUsed]
         """.stripMargin.trim
  }

  def saveService(name: String, definition: String): Unit = {
    new File(projectSrcDir).mkdirs()
    val pw = new PrintWriter(s"${projectSrcDir}/${cToPascal(name)}Service.scala", "utf-8")
    pw.println(definition)
    pw.close()
  }

}


