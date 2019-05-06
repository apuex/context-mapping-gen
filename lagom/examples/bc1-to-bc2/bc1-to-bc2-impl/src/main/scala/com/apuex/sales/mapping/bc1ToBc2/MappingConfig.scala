package com.apuex.sales.mapping.bc1ToBc2

import com.google.protobuf.util.JsonFormat


class MappingConfig {
  val registry = JsonFormat.TypeRegistry
    .newBuilder
    .add(MappingMessages.getDescriptor.getMessageTypes)
    .build

  val printer = JsonFormat.printer().usingTypeRegistry(registry)

  val parser = JsonFormat.parser().usingTypeRegistry(registry)

}
