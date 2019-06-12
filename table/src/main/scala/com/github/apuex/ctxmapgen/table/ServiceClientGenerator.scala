package com.github.apuex.ctxmapgen.table

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.table.ServiceClientGenerator._
import com.github.apuex.ctxmapgen.table.TableMappingGenerator._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent


object ServiceClientGenerator {
  def apply(fileName: String): ServiceClientGenerator = new ServiceClientGenerator(MappingLoader(fileName))

  def apply(mappingLoader: MappingLoader): ServiceClientGenerator = new ServiceClientGenerator(mappingLoader)


  def retrieveByRowid(name: String): String = {
    s"""
       |def retrieve${cToPascal(name)}ByRowid(): ServiceCall[RetrieveByRowidCmd, ${cToPascal(name)}Vo]
     """.stripMargin.trim
  }

  def create(name: String): String = {
    s"""
       |def create${cToPascal(name)}(): ServiceCall[Create${cToPascal(name)}Cmd, NotUsed]
     """.stripMargin.trim
  }

  def retrieve(name: String): String = {
    s"""
       |def retrieve${cToPascal(name)}(): ServiceCall[Retrieve${cToPascal(name)}Cmd, ${cToPascal(name)}Vo]
     """.stripMargin.trim
  }

  def query(name: String): String = {
    s"""
       |def query${cToPascal(name)}(): ServiceCall[QueryCommand, ${cToPascal(name)}ListVo]
     """.stripMargin.trim
  }

  def update(name: String): String = {
    s"""
       |def update${cToPascal(name)}(): ServiceCall[Update${cToPascal(name)}Cmd, NotUsed]
     """.stripMargin.trim
  }

  def delete(name: String): String = {
    s"""
       |def delete${cToPascal(name)}(): ServiceCall[Delete${cToPascal(name)}Cmd, NotUsed]
     """.stripMargin.trim
  }
}

class ServiceClientGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    saveService(
      srcSystem,
      generateSrcServiceClient())
    saveService(
      destSystem,
      generateDestServiceClient())
  }

  def generateSrcServiceClient(): String = {
    s"""
       |package ${modelPackage}
       |
       |import akka._
       |import akka.stream.scaladsl._
       |import com.lightbend.lagom.scaladsl.api._
       |import com.github.apuex.springbootsolution.runtime._
       |import play.api.libs.json.Json
       |
       |trait ${cToPascal(srcSystem)}Service extends Service {
       |
       |  ${indent(srcCalls(), 2)}
       |
       |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
       |
       |  override def descriptor: Descriptor = {
       |    import Service._
       |
       |    implicit val queryCommandFormat = Json.format[QueryCommand]
       |    implicit val retrieveByRowidFormat = Json.format[RetrieveByRowidCmd]
       |    ${indent(srcCallJsonFormats(), 4)}
       |
       |    named("${cToShell(srcSystem)}")
       |      .withCalls(
       |        ${indent(srcCallDescs(), 8)}
       |        pathCall("/api/events?offset", events _)
       |      ).withAutoAcl(true)
       |  }
       |}
     """.stripMargin.trim
  }

  def generateDestServiceClient(): String = {
    s"""
       |package ${modelPackage}
       |
       |import akka._
       |import akka.stream.scaladsl._
       |import com.lightbend.lagom.scaladsl.api._
       |import play.api.libs.json.Json
       |
       |trait ${cToPascal(destSystem)}Service extends Service {
       |
       |  ${indent(destCalls(), 2)}
       |
       |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
       |
       |  override def descriptor: Descriptor = {
       |    import Service._
       |
       |    ${indent(destCallJsonFormats(), 4)}
       |
       |    named("${cToShell(destSystem)}")
       |      .withCalls(
       |        ${indent(destCallDescs(), 8)}
       |        pathCall("/api/events?offset", events _)
       |      ).withAutoAcl(true)
       |  }
       |}
     """.stripMargin.trim
  }

  def srcCallJsonFormats(): String = {
    (srcTables(xml)
      .map(_.\@("name"))
      .map(x =>
        s"""
           |implicit val ${cToCamel(x)}VoFormat = Json.format[${cToPascal(x)}Vo]
         """.stripMargin.trim) ++
      srcViews(xml)
        .map(_.\@("name"))
        .map(x =>
          s"""
             |implicit val ${cToCamel(x)}VoFormat = Json.format[${cToPascal(x)}Vo]
             |implicit val ${cToCamel(x)}ListVoFormat = Json.format[${cToPascal(x)}ListVo]
           """.stripMargin.trim)
      )
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def srcCalls(): String = {
    val tables = srcTables(xml)
    val byRowids = tables
      .map(x => retrieveByRowid(x.\@("name")))

    val byKeys = tables
      .map(x => x.child.filter(_.label == "view"))
      .flatMap(x => x)
      .map(x => query(x.\@("name")))

    (byRowids ++ byKeys)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def srcCallDescs(): String = {
    val tables = srcTables(xml)
    val byRowids = tables
      .map(_.\@("name"))
      .map(x =>
        s"""
           |pathCall("/api/retrieve-${cToShell(x)}-by-rowid", retrieve${cToPascal(x)}ByRowid _),
         """.stripMargin.trim)

    val byKeys = tables
      .map(x => x.child.filter(_.label == "view"))
      .flatMap(x => x)
      .map(_.\@("name"))
      .map(x =>
        s"""
           |pathCall("/api/query-${cToShell(x)}", query${cToPascal(x)} _),
         """.stripMargin.trim
      )

    (byRowids ++ byKeys)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def destCallJsonFormats(): String = {
    destTables(xml)
      .map(_.\@("name"))
      .map(cToPascal(_))
      .map(x =>
        s"""
           |implicit val create${x}CmdFormat = Json.format[Create${x}Cmd]
           |implicit val update${x}CmdFormat = Json.format[Update${x}Cmd]
           |implicit val delete${x}CmdFormat = Json.format[Delete${x}Cmd]
         """.stripMargin.trim)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def destCalls(): String = {
    destTables(xml)
      .map(_.\@("name"))
      .map(x =>
        s"""
           |${create(x)}
           |${update(x)}
           |${delete(x)}
         """.stripMargin.trim)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def destCallDescs(): String = {
    destTables(xml)
      .map(_.\@("name"))
      .map(x =>
        s"""
           |pathCall("/api/create-${cToShell(x)}", create${cToPascal(x)} _),
           |pathCall("/api/update-${cToShell(x)}", update${cToPascal(x)} _),
           |pathCall("/api/delete-${cToShell(x)}", delete${cToPascal(x)} _),
         """.stripMargin.trim)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def saveService(name: String, definition: String): Unit = {
    new File(apiSrcDir).mkdirs()
    val pw = new PrintWriter(s"${apiSrcDir}/${cToPascal(name)}Service.scala", "utf-8")
    pw.println(definition)
    pw.close()
  }

}


