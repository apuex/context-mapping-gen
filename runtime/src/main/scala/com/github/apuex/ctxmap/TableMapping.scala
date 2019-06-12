package com.github.apuex.ctxmap

trait TableMapping {
  def create(tableName: String, rowid: String): Unit
  def update(tableName: String, rowid: String): Unit
  def delete(tableName: String, rowid: String): Unit
}
