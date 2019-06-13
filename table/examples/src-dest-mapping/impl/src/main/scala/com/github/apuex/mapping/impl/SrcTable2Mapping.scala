package com.github.apuex.mapping.impl

import com.github.apuex.mapping._
import com.github.apuex.ctxmap._
import com.github.apuex.springbootsolution.runtime.QueryCommand
import com.github.apuex.springbootsolution.runtime.QueryCommandMethods.andCommand
import scala.concurrent.ExecutionContext

class SrcTable2Mapping (
    src: SrcService,
    dest: DestService,
    deleteQueue: StashedQueue,
    implicit val ec: ExecutionContext
  ) extends TableMapping {
  import deleteQueue._

  val tableName = "src_table_2"

  override def create(rowid: String): Unit = {
    src.retrieveSrcTable2ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        stash(tableName, rowid, DeleteDestTable3Cmd(t.col1, t.col2))
        dest.createDestTable3().invoke(CreateDestTable3Cmd(t.col1, t.col2, t.col3, t.col4))
        stash(tableName, rowid, DeleteDestTable4Cmd(t.col1, t.col2))
        dest.createDestTable4().invoke(CreateDestTable4Cmd(t.col1, t.col2, t.col3, t.col4))
      })
  }

  override def update(rowid: String): Unit = {
    src.retrieveSrcTable2ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        dest.updateDestTable3().invoke(UpdateDestTable3Cmd(t.col1, t.col2, t.col3, t.col4))
        dest.updateDestTable4().invoke(UpdateDestTable4Cmd(t.col1, t.col2, t.col3, t.col4))
      })
  }

  override def delete(cmds: Seq[Any]): Unit = {
    cmds.foreach({
      case x: DeleteDestTable3Cmd =>
        dest.deleteDestTable3().invoke(x)
      case x: DeleteDestTable4Cmd =>
        dest.deleteDestTable4().invoke(x)
    })
  }

  
}
