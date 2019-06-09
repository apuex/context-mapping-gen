package com.github.apuex.hello

import com.github.apuex.hello.TableRowChangeType.TableRowChangeType

object TableRowChangeType extends Enumeration {
  type TableRowChangeType = Value
  val INSERT, UPDATE, DELETE = Value
}

case class TableRowChangedEvt(tableName: String, rowid: String, changeType: TableRowChangeType)
case class RetrieveByRowidCmd(rowid: String)

trait TableMapping {
  def create(tableName: String, rowid: String): Unit
  def update(tableName: String, rowid: String): Unit
  def delete(tableName: String, rowid: String): Unit
}
