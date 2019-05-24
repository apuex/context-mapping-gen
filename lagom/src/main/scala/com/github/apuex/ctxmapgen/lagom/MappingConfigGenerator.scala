package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

class MappingConfigGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  val mappingConfig =
    s"""
       |package ${apiSrcPackage}
       |
       |import java.util.Date
       |import java.util.concurrent.TimeUnit
       |
       |import akka.stream.scaladsl.Source
       |import akka.util.Timeout
       |import com.github.apuex.events.play.{EventEnvelope, EventEnvelopeProto}
       |import com.google.protobuf.any.Any
       |import play.api.libs.json._
       |import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
       |import scalapb.json4s.JsonFormat.GenericCompanion
       |import scalapb.json4s._
       |
       |import scala.concurrent.duration.Duration
       |
       |class MappingConfig {
       |  // json parser and printer
       |  val messagesCompanions = MappingsProto.messagesCompanions ++ EventEnvelopeProto.messagesCompanions
       |  val registry: TypeRegistry = messagesCompanions
       |    .foldLeft(TypeRegistry())((r, mc) => r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
       |
       |  val printer = new Printer().withTypeRegistry(registry)
       |
       |  val parser = new Parser().withTypeRegistry(registry)
       |
       |  // any packager for pack/unpack messages.
       |  def unpack(any: Any): GeneratedMessage = registry.findType(any.typeUrl)
       |    .map(_.parseFrom(any.value.newCodedInput())).get
       |
       |  implicit val duration = Duration.apply(30, TimeUnit.SECONDS)
       |  implicit val timeout = Timeout(duration)
       |  val snapshotSequenceCount: Long = 1000
       |
       |  val keepAlive = Source.fromIterator(() => new Iterator[String] {
       |    override def hasNext: Boolean = true
       |
       |    override def next(): String = s"[$${new Date()}] - keep-alive."
       |  })
       |    .throttle(1, duration)
       |    .map(_.toString)
       |
       |  def parseJson(json: String): EventEnvelope = {
       |    parser.fromJsonString[EventEnvelope](json)
       |  }
       |
       |  class MessageFormat[T <: GeneratedMessage with Message[T] : GeneratedMessageCompanion] extends OFormat[T] {
       |    override def reads(json: JsValue): JsResult[T] = {
       |      JsSuccess(parser.fromJsonString[T](json.toString()))
       |    }
       |
       |    override def writes(o: T): JsObject = Json.parse(
       |      printer.print(o)
       |    ).validate[JsObject].get
       |  }
       |
       |  def jsonFormat[T <: GeneratedMessage with Message[T] : GeneratedMessageCompanion]: OFormat[T] = new MessageFormat[T]
       |}
     """.stripMargin.trim

  def generate(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implSrcDir}/MappingConfig.scala", "utf-8")
    printWriter.println(mappingConfig)
    printWriter.close()
  }
}
