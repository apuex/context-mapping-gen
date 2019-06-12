package com.github.apuex.mapping.impl

import com.github.apuex.mapping._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.QueryCommandMethods._
import scala.concurrent.ExecutionContext

class SrcTable2Mapping (
    src: SrcService,
    dest: DestService,
    addDelete: (String, String, Any) => Unit,
    getDeletes: (String, String) => Seq[Any],
    implicit val ec: ExecutionContext
  ) extends TableMapping {

  override def create(tableName: String, rowid: String): Unit = {
    src.retrieveSrcTable2ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        addDelete(tableName, rowid, DeleteDestTable3Cmd(t.col1, t.col2))
        dest.createDestTable3().invoke(CreateDestTable3Cmd(t.col1, t.col2, t.col3, t.col4))
        addDelete(tableName, rowid, DeleteDestTable4Cmd(t.col1, t.col2))
        dest.createDestTable4().invoke(CreateDestTable4Cmd(t.col1, t.col2, t.col3, t.col4))
      })
  }

  override def update(tableName: String, rowid: String): Unit = {
    src.retrieveSrcTable2ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        dest.updateDestTable3().invoke(UpdateDestTable3Cmd(t.col1, t.col2, t.col3, t.col4))
        dest.updateDestTable4().invoke(UpdateDestTable4Cmd(t.col1, t.col2, t.col3, t.col4))
      })
  }

  override def delete(tableName: String, rowid: String): Unit = {
    getDeletes(tableName, rowid)
      .foreach({
        case x: DeleteDestTable3Cmd =>
          dest.deleteDestTable3().invoke(x)
        case x: DeleteDestTable4Cmd =>
          dest.deleteDestTable4().invoke(x)
      })
  }

  
}
