package com.github.apuex.ctxmap

trait TableMappingFactory {
  def create(
              tableName: String,
              deleteQueue: StashedQueue
            ): TableMapping
}
