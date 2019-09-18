package com.apuex.greeting.mapping.h2g

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.example.leveldb._
import com.github.apuex.events.play._
import com.google.protobuf.any.Any
import play.api.libs.json._
import scalapb.json4s.JsonFormat.GenericCompanion
import scalapb.json4s.{Parser, Printer, TypeRegistry}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

import scala.concurrent.duration.Duration

class MappingConfig {
  // json parser and printer
  val messagesCompanions = MessagesProto.messagesCompanions ++ EventEnvelopeProto.messagesCompanions
  val registry: TypeRegistry = messagesCompanions
    .foldLeft(TypeRegistry())((r, mc) => r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))

  val printer = new Printer().withTypeRegistry(registry)

  val parser = new Parser().withTypeRegistry(registry)

  // any packager for pack/unpack messages.
  def unpack(any: Any): GeneratedMessage = registry.findType(any.typeUrl)
    .map(_.parseFrom(any.value.newCodedInput())).get

  implicit val duration = Duration.apply(30, TimeUnit.SECONDS)
  implicit val timeout = Timeout(duration)
  val snapshotSequenceCount: Long = 1000

  val keepAlive = Source.fromIterator(() => new Iterator[String] {
    override def hasNext: Boolean = true

    override def next(): String = "{}"
  })
    .throttle(1, duration)
    .map(_.toString)

  def parseJson(json: String): EventEnvelope = {
    parser.fromJsonString[EventEnvelope](json)
  }

  class MessageFormat[T <: GeneratedMessage with Message[T] : GeneratedMessageCompanion] extends OFormat[T] {
    override def reads(json: JsValue): JsResult[T] = {
      JsSuccess(parser.fromJsonString[T](json.toString()))
    }

    override def writes(o: T): JsObject = Json.parse(
      printer.print(o)
    ).validate[JsObject].get
  }

  def jsonFormat[T <: GeneratedMessage with Message[T] : GeneratedMessageCompanion]: OFormat[T] = new MessageFormat[T]
}
