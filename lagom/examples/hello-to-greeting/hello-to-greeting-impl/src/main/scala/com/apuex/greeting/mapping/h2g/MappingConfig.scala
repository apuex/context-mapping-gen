package com.apuex.greeting.mapping.h2g

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.stream.scaladsl.Source
import com.example.leveldb._
import com.github.apuex.events.play._
import com.github.apuex.protobuf.serializer.AnyPackagerBuilder
import com.google.protobuf.util.JsonFormat

import scala.concurrent.duration.Duration

class MappingConfig {
  // json parser and printer
  val registry = JsonFormat.TypeRegistry
    .newBuilder
    .add(MessagesProto.getDescriptor.getMessageTypes)
    .add(EventEnvelopeProto.getDescriptor.getMessageTypes)
    .build

  val printer = JsonFormat.printer().usingTypeRegistry(registry)

  val parser = JsonFormat.parser().usingTypeRegistry(registry)

  // any packager for pack/unpack messages.
  val packager = AnyPackagerBuilder.builder()
    .withFileDescriptorProto(MessagesProto.getDescriptor.toProto)
    .withFileDescriptorProto(EventEnvelopeProto.getDescriptor.toProto)
    .withStringRegistry()
    .build()

  val snapshotAfter = 1000
  val keepAlive = Source.fromIterator(() => new Iterator[String] {
    override def hasNext: Boolean = true

    override def next(): String = s"[${new Date()}] - keep-alive."
  })
    .throttle(1, Duration.apply(30, TimeUnit.SECONDS))
    .map(_.toString)

  def parseJson(json: String): EventEnvelope = {
    val builder = EventEnvelope.newBuilder
    parser.merge(json, builder)
    builder.build
  }
}
