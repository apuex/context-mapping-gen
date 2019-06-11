package com.github.apuex.ctxmapgen.table

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.table.TableMappingGenerator._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node

object TableMappingGenerator {
  def apply(mappingFile: String): TableMappingGenerator = TableMappingGenerator(MappingLoader(mappingFile))

  def apply(mappingLoader: MappingLoader): TableMappingGenerator = new TableMappingGenerator(mappingLoader)

  def destTableNames(view: Node): Seq[String] = {
    view.child.filter(_.label == "dest-table")
      .map(_.\@("name")) ++
      view.child.filter(_.label != "dest-table")
        .map(destTableNames(_))
        .flatMap(x => x)
  }

  def columns(view: Node): Seq[String] = {
    view.child.filter(p => p.label == "column")
      .sortWith((l, r) => l.\@("no") < r.\@("no"))
      .map(_.\@("name"))
  }

  def filterKeyColumns(view: Node): Seq[(String, String)] = {
    view.child.filter(x => x.label == "filter-key")
      .flatMap(x => x.child.filter(p => p.label == "column"))
      .map(x => (x.\@("name"), x.\@("type")))
  }

  def filterKeyColumnNames(view: Node): Seq[String] = {
    val keys = filterKeyColumns(view)
      .map(_._1)
    val cols = columns(view)

    if (cols.isEmpty) keys else cols.filter(keys.contains(_))
  }

  def filterKeyParamsDef(keyColumns: Seq[(String, String)]): String = {
    keyColumns.map(x => s"${cToCamel(x._1)}: ${cToPascal(x._2)}")
      .reduceOption((l, r) => s"${l}, ${r}")
      .getOrElse("")
  }

  def filterKeyParamsMap(keyColumnNames: Seq[String]): String = {
    keyColumnNames.map(cToCamel(_))
      .map(x =>
        s"""
           |"${x}" -> ${x}
         """.stripMargin.trim)
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")
  }

  def paramSubstitutions(paramNames: Seq[String], from: String): String = {
    paramNames
      .map(cToCamel(_))
      .map(x => s"${from}.${x}")
      .reduceOption((l, r) => s"${l}, ${r}")
      .getOrElse("")
  }

  def by(paramNames: Seq[String]): String = {
    paramNames
      .map(cToPascal(_))
      .reduceOption((l, r) => s"${l}${r}")
      .getOrElse("")
  }

  def queryCommand(view: Node): String = {
    val keyColumns = filterKeyColumns(view)
    val keys = filterKeyColumnNames(view)
    val map = if (keys.isEmpty) "Map()"
    else
      s"""
         |Map(
         |  ${indent(filterKeyParamsMap(keys), 2)}
         |)
     """.stripMargin.trim
    s"""
       |def query${cToPascal(view.\@("name"))}By${by(keys)}Cmd(${filterKeyParamsDef(keyColumns)}): QueryCommand = andCommand(
       |  ${indent(map, 2)}
       |)
     """.stripMargin.trim
  }

  def queryCommands(view: Node): String = {
    view
      .flatMap(x => x.child.filter(_.label == "view"))
      .map(queryCommand(_))
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def deletes(tableNames: Seq[String]): String = {
    tableNames
      .map(x =>
        s"""
           |case x: Delete${cToPascal(x)}Cmd =>
           |  dest.delete${cToPascal(x)}().invoke(x)
         """.stripMargin.trim)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }
}

class TableMappingGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    tableMappings(xml)
  }

  def tableMappings(xml: Node): Unit = {
    xml.filter(x => x.label == "src-table")
      .map(generateTableMapping(_))
      .foreach(x => saveTableMappingImpl(x._1, x._2))
  }

  def saveTableMappingImpl(tableName: String, mappingImpl: String): Unit = {
    new File(projectSrcDir).mkdirs()
    val pw = new PrintWriter(s"${projectSrcDir}/${cToPascal(tableName)}Mapping.scala", "utf-8")
    pw.println(mappingImpl)
    pw.close()
  }

  def generateTableMapping(table: Node): (String, String) = {
    val tableName = table.\@("name")
    val mappingImpl =
      s"""
         |package ${modelPackage}
         |
         |import com.github.apuex.springbootsolution.runtime._
         |import com.github.apuex.springbootsolution.runtime.QueryCommandMethods._
         |import scala.concurrent.ExecutionContext
         |
         |class ${cToPascal(tableName)}Mapping (
         |    ${cToCamel(srcSystem)}: ${cToPascal(srcSystem)}Service,
         |    ${cToCamel(destSystem)}: ${cToPascal(destSystem)}Service,
         |    addDelete: (String, String, Any) => Unit,
         |    getDeletes: (String, String) => Seq[Any],
         |    implicit val ec: ExecutionContext
         |  ) extends TableMapping {
         |
         |  override def create(tableName: String, rowid: String): Unit = {
         |    ${indent(insertFromRowId(table), 4)}
         |  }
         |
         |  override def update(tableName: String, rowid: String): Unit = {
         |    ${indent(updateFromRowId(table), 4)}
         |  }
         |
         |  override def delete(tableName: String, rowid: String): Unit = {
         |    ${indent(deleteFromRowId(table), 4)}
         |  }
         |
         |  ${indent(queryCommands(table), 2)}
         |}
     """.stripMargin.trim
    (tableName, mappingImpl)
  }

  def insertFromRowId(table: Node): String = {
    val tableName = table.\@("name")
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}ByRowid().invoke(RetrieveByRowidCmd(rowid))
       |  .map(t => {
       |    ${indent(insertFromTableMapping(table, "t"), 4)}
       |  })
     """.stripMargin.trim
  }

  def insertFromTableMapping(table: Node, from: String): String = {
    val tables = insertDestinationTables(table, from)
    val views = table.child.filter(x => x.label == "view")
      .map(insertFromView(_))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
    s"""
       |${tables}
       |${views}
     """.stripMargin.trim
  }

  def insertFromView(view: Node): String = {
    val tableName = view.\@("name")
    val keys = filterKeyColumnNames(view)
    s"""
       |${srcSystem}.query${cToPascal(tableName)}().invoke(query${cToPascal(tableName)}By${by(keys)}Cmd(${paramSubstitutions(keys, "t")}))
       |  .map(_.items.map(v => {
       |    ${indent(insertDestinationTables(view, "v"), 4)}
       |  }))
     """.stripMargin.trim
  }

  def insertDestinationTables(view: Node, from: String): String = {
    view.child.filter(_.label == "dest-table")
      .map(x => insertDestinationTable(x, from))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def insertDestinationTable(table: Node, from: String): String = {
    val tableName = table.\@("name")
    s"""
       |addDelete(tableName, rowid, Delete${cToPascal(tableName)}Cmd(${paramSubstitutions(filterKeyColumnNames(table), from)}))
       |${cToCamel(destSystem)}.create${cToPascal(tableName)}().invoke(Create${cToPascal(tableName)}Cmd(${paramSubstitutions(columns(table), from)}))
     """.stripMargin.trim
  }

  def updateFromRowId(table: Node): String = {
    val tableName = table.\@("name")
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}ByRowid().invoke(RetrieveByRowidCmd(rowid))
       |  .map(t => {
       |    ${indent(updateFromTableMapping(table, "t"), 4)}
       |  })
     """.stripMargin.trim
  }

  def updateFromTableMapping(table: Node, from: String): String = {
    val tables = updateDestinationTables(table, from)
    val views = table.child.filter(x => x.label == "view")
      .map(updateFromView(_))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
    s"""
       |${tables}
       |${views}
     """.stripMargin.trim
  }

  def updateFromView(view: Node): String = {
    val tableName = view.\@("name")
    val keys = filterKeyColumnNames(view)
    s"""
       |${srcSystem}.query${cToPascal(tableName)}().invoke(query${cToPascal(tableName)}By${by(keys)}Cmd(${paramSubstitutions(keys, "t")}))
       |  .map(_.items.map(v => {
       |    ${indent(updateDestinationTables(view, "v"), 4)}
       |  }))
     """.stripMargin.trim
  }

  def updateDestinationTables(view: Node, from: String): String = {
    view.child.filter(_.label == "dest-table")
      .map(x => updateDestinationTable(x, from))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def updateDestinationTable(table: Node, from: String): String = {
    val tableName = table.\@("name")
    s"""
       |${cToCamel(destSystem)}.update${cToPascal(tableName)}().invoke(Update${cToPascal(tableName)}Cmd(${paramSubstitutions(columns(table), from)}))
     """.stripMargin.trim
  }

  def deleteFromRowId(table: Node): String = {
    s"""
       |getDeletes(tableName, rowid)
       |  .foreach({
       |    ${indent(deletes(destTableNames(table)), 4)}
       |  })
     """.stripMargin.trim
  }

}


