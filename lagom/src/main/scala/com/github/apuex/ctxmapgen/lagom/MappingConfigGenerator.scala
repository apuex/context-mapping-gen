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
       |import com.github.apuex.events.play._
       |import com.github.apuex.protobuf.serializer.AnyPackagerBuilder
       |import com.google.protobuf.Message
       |import com.google.protobuf.util.JsonFormat
       |import play.api.libs.json._
       |
       |import scala.concurrent.duration.Duration
       |
       |class MappingConfig {
       |  // json parser and printer
       |  val registry = JsonFormat.TypeRegistry
       |    .newBuilder
       |    // TODO: add your protobuf message descriptors here.
       |    .add(MappingMessages.getDescriptor.getMessageTypes)
       |    .add(EventEnvelopeProto.getDescriptor.getMessageTypes)
       |    .build
       |
       |  val printer = JsonFormat.printer().usingTypeRegistry(registry)
       |
       |  val parser = JsonFormat.parser().usingTypeRegistry(registry)
       |
       |  // any packager for pack/unpack messages.
       |  val packager = AnyPackagerBuilder.builder()
       |    // TODO: add your protobuf message descriptors here.
       |    .withFileDescriptorProto(MappingMessages.getDescriptor.toProto)
       |    .withFileDescriptorProto(EventEnvelopeProto.getDescriptor.toProto)
       |    .withStringRegistry()
       |    .build()
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
       |    val builder = EventEnvelope.newBuilder
       |    parser.merge(json, builder)
       |    builder.build
       |  }
       |
       |  class MessageFormat[T<: Message](val clazz: Class[T]) extends OFormat[T] {
       |    val builderMethod = clazz.getDeclaredMethod("newBuilder")
       |    override def reads(json: JsValue): JsResult[T] = {
       |      val builder = builderMethod.invoke(null).asInstanceOf[Message.Builder]
       |      parser.merge(json.toString(), builder)
       |      JsSuccess(builder.build().asInstanceOf[T])
       |    }
       |
       |    override def writes(o: T): JsObject = Json.parse(
       |      printer.print(o)
       |    ).validate[JsObject].get
       |  }
       |
       |  def jsonFormat[T<: Message](clazz: Class[T]): OFormat[T] = new MessageFormat(clazz)
       |}
     """.stripMargin.trim

  def generate(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implSrcDir}/MappingConfig.scala", "utf-8")
    printWriter.println(mappingConfig)
    printWriter.close()
  }
}
