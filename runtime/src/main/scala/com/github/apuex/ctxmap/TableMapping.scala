package com.github.apuex.ctxmap

trait TableMapping {
  def create(rowid: String): Unit

  def update(rowid: String): Unit

  def delete(cmds: Seq[Any]): Unit
}
