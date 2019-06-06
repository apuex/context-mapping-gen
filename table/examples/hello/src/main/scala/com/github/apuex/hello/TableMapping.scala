package com.github.apuex.hello

trait TableMapping {
  def insert(tableName: String, rowid: String): Unit
  def update(tableName: String, rowid: String): Unit
  def delete(tableName: String, rowid: String): Unit
}
