package com.github.apuex.mapping.impl

import com.github.apuex.ctxmap._
import com.github.apuex.mapping._

import scala.concurrent.ExecutionContext

object TableMappingImpl {

  def create(srcService: SrcService,
             destService: DestService,
             deleteQueue: StashedQueue,
             ec: ExecutionContext
            ): Map[String, TableMapping] = {
    Map(
      "src_table_1" -> new SrcTable1Mapping(srcService, destService, deleteQueue, ec),
      "src_table_1" -> new SrcTable1Mapping(srcService, destService, deleteQueue, ec)
    )
  }
}
