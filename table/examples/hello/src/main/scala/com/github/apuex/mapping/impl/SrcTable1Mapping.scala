package com.github.apuex.mapping.impl

import com.github.apuex.mapping._
import com.github.apuex.ctxmap._
import com.github.apuex.springbootsolution.runtime.QueryCommand
import com.github.apuex.springbootsolution.runtime.QueryCommandMethods.andCommand
import scala.concurrent.ExecutionContext

class SrcTable1Mapping (
    src: SrcService,
    dest: DestService,
    deleteQueue: StashedQueue,
    implicit val ec: ExecutionContext
  ) extends TableMapping {
  import deleteQueue._

  val tableName = "src_table_1"

  override def create(rowid: String): Unit = {
    src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        stash(tableName, rowid, DeleteDestTable1Cmd(t.col1, t.col2))
        dest.createDestTable1().invoke(CreateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
        src.querySrcView1().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
          .map(_.items.map(v => {
            stash(tableName, rowid, DeleteDestTable2Cmd(v.col1, v.col2))
            dest.createDestTable2().invoke(CreateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
          }))
        src.querySrcView2().invoke(querySrcView2ByCol1Cmd(t.col1))
          .map(_.items.map(v => {
            stash(tableName, rowid, DeleteDestTable5Cmd(v.col1))
            dest.createDestTable5().invoke(CreateDestTable5Cmd(v.col1, v.col2, v.col3))
          }))
      })
  }

  override def update(rowid: String): Unit = {
    src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        dest.updateDestTable1().invoke(UpdateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
        src.querySrcView1().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
          .map(_.items.map(v => {
            dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
          }))
        src.querySrcView2().invoke(querySrcView2ByCol1Cmd(t.col1))
          .map(_.items.map(v => {
            dest.updateDestTable5().invoke(UpdateDestTable5Cmd(v.col1, v.col2, v.col3))
          }))
      })
  }

  override def delete(cmds: Seq[Any]): Unit = {
    cmds.foreach({
      case x: DeleteDestTable1Cmd =>
        dest.deleteDestTable1().invoke(x)
      case x: DeleteDestTable2Cmd =>
        dest.deleteDestTable2().invoke(x)
      case x: DeleteDestTable5Cmd =>
        dest.deleteDestTable5().invoke(x)
    })
  }

  def querySrcView1ByCol1Col2Cmd(col1: String, col2: Long): QueryCommand = andCommand(
    Map(
      "col1" -> col1,
      "col2" -> col2
    )
  )

  def querySrcView2ByCol1Cmd(col1: String): QueryCommand = andCommand(
    Map(
      "col1" -> col1
    )
  )
}
