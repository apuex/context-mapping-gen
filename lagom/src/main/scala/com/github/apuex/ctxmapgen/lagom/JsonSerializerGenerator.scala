package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

class JsonSerializerGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  val scalapbJson =
    s"""
       |package ${apiSrcPackage}
       |
       |import com.github.apuex.events.play.{EventEnvelope, EventEnvelopeProto}
       |import com.google.protobuf.any.Any
       |import play.api.libs.json._
       |import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
       |import scalapb.json4s.JsonFormat.GenericCompanion
       |import scalapb.json4s._
       |
       |object ScalapbJson {
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
    new File(apiSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${apiSrcDir}/ScalapbJson.scala", "utf-8")
    printWriter.println(scalapbJson)
    printWriter.close()
  }
}
