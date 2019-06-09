package com.github.apuex.hello

import scala.concurrent.ExecutionContext

class HelloTableMapping (src: SrcService, dest: DestService, implicit val ec: ExecutionContext) extends TableMapping {

  override def create(tableName: String, rowid: String): Unit = {
    src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        dest.createDestTable1().invoke(CreateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
        src.retrieveSrcView1().invoke(RetrieveSrcView1Cmd(t.col1, t.col2))
          .map(v => {
            dest.createDestTable2().invoke(CreateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
          })
        src.retrieveSrcView2().invoke(RetrieveSrcView2Cmd(t.col1))
          .map(v => {
            dest.createDestTable5().invoke(CreateDestTable5Cmd(v.col1, v.col2, v.col3))
          })
      })
  }

  override def update(tableName: String, rowid: String): Unit = {
    src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
      .map(t => {
        dest.updateDestTable1().invoke(UpdateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
        src.retrieveSrcView1().invoke(RetrieveSrcView1Cmd(t.col1, t.col2))
          .map(v => {
            dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
          })
        src.retrieveSrcView2().invoke(RetrieveSrcView2Cmd(t.col1))
          .map(v => {
            dest.updateDestTable5().invoke(UpdateDestTable5Cmd(v.col1, v.col2, v.col3))
          })
      })
  }

  override def delete(tableName: String, rowid: String): Unit = {

  }
}