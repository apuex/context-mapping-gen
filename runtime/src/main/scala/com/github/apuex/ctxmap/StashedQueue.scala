package com.github.apuex.ctxmap

trait StashedQueue {
  def stash(tableName: String, rowid: String, cmd: Any): Unit
}
