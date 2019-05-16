package com.github.apuex.ctxmapgen.lagom

import java.io.PrintWriter

import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

import scala.xml.Node

class ContextMappingGenerator(mappingFile: String) {
  val model = MappingLoader(mappingFile)

  import model._

  val projectGenerator = new ProjectGenerator(model)
  val applicationConfGenerator = new ApplicationConfGenerator(model)
  val applicationLoaderGenerator = new ApplicationLoaderGenerator(model)
  val serviceGenerator = new ServiceGenerator(model)
  val mappingConfigGenerator = new MappingConfigGenerator(model)

  def generate(): Unit = {
    projectGenerator.generate()
    applicationConfGenerator.generate()
    applicationLoaderGenerator.generate()
    serviceGenerator.generate()
    mappingConfigGenerator.generate()
    generateServiceMappings()
  }

  private def generateServiceMappings(): Unit = {
    val mappingName = cToPascal(s"${srcSystem}_${destSystem}_${mapping}")
    val printWriter = new PrintWriter(s"${implSrcDir}/${mappingName}.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${implSrcPackage}\n")
    printWriter.println(
      s"""
         |import java.util.concurrent.TimeUnit
         |
         |import akka._
         |import akka.actor._
         |import akka.persistence._
         |import akka.stream._
         |import akka.stream.scaladsl._
         |import ${apiSrcPackage}._
         |import com.github.apuex.events.play._
         |import com.google.protobuf.Message
         |
         |import scala.collection.JavaConverters._
         |import scala.concurrent._
         |import scala.concurrent.duration._
       """.stripMargin
        .trim)

    printWriter.println()
    // companion object declaration
    printWriter.println(
      s"""
         |object ${mappingName} {
         |  def name = "${mappingName}"
         |}
       """.stripMargin
        .trim)

    printWriter.println()
    // begin class declaration
    printWriter.println(
      s"""
         |class ${mappingName} (
         |    ${indent(dependency(), 4)}
         |  )
         |  extends PersistentActor
         |    with ActorLogging {
         |
         |  import config._
         |
         |  implicit val executionContext = context.system.dispatcher
         |  implicit val materializer = ActorMaterializer()
         |
         |  override def persistenceId: String = OrderInventoryMapping.name
         |
         |  // state
         |  var offset: Option[String] = None
         """.stripMargin
        .trim)

    printWriter.println(s"${indent(receiveRecover(), 2, true)}\n")
    printWriter.println(s"${indent(receiveCommand(), 2, true)}\n")
    printWriter.println(s"${indent(updateState(), 2, true)}\n")
    printWriter.println(s"${indent(mapEvent(), 2, true)}\n")
    printWriter.println(s"${indent(subscribe(), 2, true)}\n")

    // end class declaration
    printWriter.println("}")
    printWriter.close()
  }

  private def receiveRecover(): String = {
    s"""
       |override def receiveRecover: Receive = {
       |  case SnapshotOffer(metadata: SnapshotMetadata, snapshot: String) =>
       |    offset = Some(snapshot)
       |  case _: RecoveryCompleted =>
       |    // TODO: connect to source service and start event subscription.
       |    log.info("recover complete.")
       |    subscribe()
       |  case x =>
       |    updateState(x)
       |}
     """.stripMargin
      .trim
  }

  private def receiveCommand(): String = {
    s"""
       |override def receiveCommand: Receive = {
       |  case x: EventEnvelope =>
       |    // TODO: process event
       |    mapEvent(x.getOffset)(packager.unpack(x.getEvent))
       |  case x =>
       |    log.info("unhandled command: {}", x)
       |}
     """.stripMargin
      .trim
  }

  private def updateState(): String = {
    s"""
       |private def updateState: (Any => Unit) = {
       |  case x: String =>
       |    offset = Some(x)
       |    log.info("unhandled update state: {}", x)
       |}
     """.stripMargin
      .trim
  }

  private def subscribe(): String = {
    s"""
       |private def subscribe(): Unit = {
       |  log.info("connecting...")
       |  order.events(offset)
       |    .invoke(
       |      keepAlive
       |    )
       |    .map(s => {
       |      log.info("connected.")
       |      s.map(parseJson)
       |        .recover({
       |          case x =>
       |            log.error(x, "broken pipe")
       |            context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
       |        }).runWith(Sink.actorRef(self, Done))
       |    })
       |    .recover({
       |      case t: Throwable =>
       |        log.error(t, "connect to event stream failed")
       |        context.system.scheduler.scheduleOnce(30.seconds)(subscribe)
       |    })
       |}
     """
      .stripMargin
      .trim
  }

  private def mapEvent(): String = {
    s"""
       |private def mapEvent(offset: String): (Message => Unit) = {
       |  // TODO: add message mappings here.
       |  ${indent(mapEvent(xml), 2)}
       |  case x =>
       |    log.debug("unhandled: {}", x)
       |}
     """
      .stripMargin
      .trim
  }

  private def dependency(): String = {
    val services: String = applicationLoaderGenerator.serviceCalls
      .map(_._1)
      .map(x => s"val ${cToCamel(x)}: ${cToPascal(s"${x}_${service}")}")
      .reduce((l, r) => s"${l},\n${r}")
    s"""
       |val config: MappingConfig,
       |${services}
       """
      .stripMargin
      .trim
  }

  private def mapEvent(node: Node): String = {
    node.child.filter(x => x.label == "map")
      .map(x => {
        s"""case ${cToCamel(x.\@("alias"))}: ${cToPascal(x.\@("message"))} =>
           |  ${indent(mapEventImpl(x), 2)}
         """
          .stripMargin
          .trim
      })
      .reduce((l, r) => s"${l}\n${r}")
  }

  private def mapEventImpl(node: Node): String = {
    ""
  }
}
