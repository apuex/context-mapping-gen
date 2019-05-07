package com.apuex.sales.mapping.bc1ToBc2

import com.github.apuex.events.play.EventEnvelopeProto
import com.github.apuex.protobuf.serializer.AnyPackagerBuilder
import com.google.protobuf.util.JsonFormat


class MappingConfig {
  // json parser and printer
  val registry = JsonFormat.TypeRegistry
    .newBuilder
    .add(MappingMessages.getDescriptor.getMessageTypes)
    .add(EventEnvelopeProto.getDescriptor.getMessageTypes)
    .build

  val printer = JsonFormat.printer().usingTypeRegistry(registry)

  val parser = JsonFormat.parser().usingTypeRegistry(registry)

  // any packager for pack/unpack messages.
  val packager = AnyPackagerBuilder.builder()
    .withFileDescriptorProto(MappingMessages.getDescriptor.toProto)
    .withFileDescriptorProto(EventEnvelopeProto.getDescriptor.toProto)
    .withStringRegistry()
    .build()
}
