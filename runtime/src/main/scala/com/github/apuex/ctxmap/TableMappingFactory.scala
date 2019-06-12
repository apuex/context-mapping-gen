package com.github.apuex.ctxmap

trait TableMappingFactory {
  def create(
              tableName: String,
              addDelete: (String, String, Any) => Unit,
              getDeletes: (String, String) => Seq[Any],
            ): TableMapping
}
