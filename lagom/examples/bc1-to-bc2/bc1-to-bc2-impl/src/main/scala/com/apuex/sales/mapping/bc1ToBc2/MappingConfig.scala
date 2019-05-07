package com.apuex.sales.mapping.bc1ToBc2

import java.util.concurrent.TimeUnit

import akka.stream.scaladsl.Source
import com.github.apuex.events.play._
import com.github.apuex.protobuf.serializer.AnyPackagerBuilder
import com.google.protobuf.util.JsonFormat

import scala.concurrent.duration.Duration


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

  val keepAlive = Source(Long.MinValue to Long.MaxValue)
    .throttle(1, Duration.apply(30, TimeUnit.SECONDS))
    .map(_.toString)

  def parseJson(json: String): EventEnvelope = {
    val builder = EventEnvelope.newBuilder
    parser.merge(json, builder)
    builder.build
  }
}
