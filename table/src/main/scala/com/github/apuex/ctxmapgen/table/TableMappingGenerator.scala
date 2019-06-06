package com.github.apuex.ctxmapgen.table

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.xml.Node

object TableMappingGenerator {
  def apply(mappingFile: String): TableMappingGenerator = TableMappingGenerator(MappingLoader(mappingFile))

  def apply(mappingLoader: MappingLoader): TableMappingGenerator = new TableMappingGenerator(mappingLoader)
}

class TableMappingGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    generateTableMappings()
  }

  def generateTableMappings(): Unit = {
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
         |class ${cToPascal(tableName)}Mapping (src: ${cToPascal(srcSystem)}Service, dest: ${cToPascal(destSystem)}Service) extends TableMapping {
         |
         |  override def insert(tableName: String, rowid: String): Unit = {
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
         |}
     """.stripMargin.trim
    (tableName, mappingImpl)
  }

  def insertFromRowId(table: Node): String = {
    val tableName = table.\@("name")
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}ByRowid().invoke(RetrieveByRowidCmd(evt.rowid))
       |  .map(t => {
       |    ${indent(insertFromTableMapping(table), 4)}
       |  })
     """.stripMargin.trim
  }

  def insertFromTableMapping(table: Node): String = {
    (table.child.filter(x => x.label == "dest-table")
      .map(insertDestinationTable(_, "t")) ++
      table.child.filter(x => x.label == "view")
        .map(insertFromView(_)))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def insertFromView(view: Node): String = {
    val tableName = view.\@("name")
    val keys = keyColumns(view)
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}().invoke(Retrieve${cToPascal(tableName)}Cmd(${paramSubstitutions(keys, "t")}))
       |  .map(v => ${view.child.filter(_.label == "dest-table").map(insertDestinationTable(_, "v"))})
     """.stripMargin.trim
  }

  def insertDestinationTable(table: Node, from: String): String = {
    val tableName = table.\@("name")
    s"""
       |dest.create${cToPascal(tableName)}().invoke(Create${cToPascal(tableName)}Cmd(${paramSubstitutions(columns(table), from)}))
     """.stripMargin.trim
  }

  def updateFromRowId(table: Node): String = {
    val tableName = table.\@("name")
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}ByRowid().invoke(RetrieveByRowidCmd(evt.rowid))
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
    val keys = keyColumns(view)
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}().invoke(Retrieve${cToPascal(tableName)}Cmd(${paramSubstitutions(keys, "t")}))
       |  .map(v => {
       |    ${indent(updateDestinationTables(view, "v"), 4)}
       |  })
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
       |dest.update${cToPascal(tableName)}().invoke(Update${cToPascal(tableName)}Cmd(${paramSubstitutions(columns(table), from)}))
     """.stripMargin.trim
  }

  def deleteFromRowId(table: Node): String = {
    val tableName = table.\@("name")
    s"""
       |
     """.stripMargin.trim
  }

  def columns(view: Node): Seq[String] = {
    view.child.filter(p => p.label == "column")
      .sortWith((l, r) => l.\@("no") < r.\@("no"))
      .map(_.\@("name"))
  }

  def keyColumns(view: Node): Seq[String] = {
    val keys = view.child.filter(x => x.label == "key")
      .flatMap(x => x.child.filter(p => p.label == "column"))
      .map(_.\@("name"))
    val cols = columns(view)

    if (cols.isEmpty) keys else cols.filter(keys.contains(_))
  }

  def paramSubstitutions(paramNames: Seq[String], from: String): String = {
    paramNames
      .map(cToCamel(_))
      .map(x => s"${from}.${x}")
      .reduceOption((l, r) => s"${l}, ${r}")
      .getOrElse("")
  }
}


