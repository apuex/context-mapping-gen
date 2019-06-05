package com.github.apuex.ctxmapgen.table

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
      .map(generateMapRowId(_))
  }

  def generateMapRowId(table: Node): String = {
    val tableName = table.\@("name")
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}ByRowid().invoke(RetrieveByRowidCmd(x.rowid))
       |  .map(x => {
       |    ${indent(generateSourceTableMapping(table), 4)}
       |  })
     """.stripMargin.trim
  }

  def generateSourceTableMapping(table: Node): String = {
    (table.child.filter(x => x.label == "dest-table")
      .map(generateDestinationTableMapping(_, table)) ++
      table.child.filter(x => x.label == "view")
        .map(generateSourceViewMapping(_, table)))
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def generateSourceViewMapping(view: Node, from: Node): String = {
    val tableName = view.\@("name")
    val keys = keyColumns(view)
    s"""
       |${srcSystem}.retrieve${cToPascal(tableName)}().invoke(Retrieve${cToPascal(tableName)}Cmd(${generateParamSubstitution(keys)}))
       |  .map(x => ${view.child.filter(_.label == "dest-table").map(generateDestinationTableMapping(_, view))})
     """.stripMargin.trim
  }

  def generateDestinationTableMapping(table: Node, from: Node): String = {
    s"""
       |
     """.stripMargin.trim
  }

  def keyColumns(view: Node): Seq[String] = {
    val keys = view.filter(x => x.label == "key")
      .flatMap(x => x.child.filter(p => p.label == "column"))
      .map(_.\@("name"))
      .toSet
    view.filter(_.label == "column")
      .sortWith((l, r) => l.\@("no") < r.\@("no"))
      .map(_.\@("name"))
      .filter(keys.contains(_))
  }

  def generateParamSubstitution(paramNames: Seq[String]): String = {
    paramNames
      .reduceOption((l, r) => s"${l}, ${r}")
      .getOrElse("")
  }
}


