package com.github.apuex.ctxmapgen.lagom

import java.io.PrintWriter

import com.github.apuex.ctxmapgen.lagom.MappingLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

import scala.xml.Node

class ContextMappingGenerator(mappingFile: String) {
  val model = MappingLoader(mappingFile)

  import model._

  def generate() : Unit = {
    new ProjectGenerator(model).generate()
    new ApplicationConfGenerator(model).generate()
    new ServiceGenerator(model).generate()
    new MappingConfigGenerator(model).generate()
    generateServiceMappings()
  }

  private def generateServiceMappings(): Unit = {
    model.xml.child.filter(x => x.label == "service")
      .foreach(x => serviceMapping(x))
  }

  private def serviceMapping(service: Node): Unit = {
    val from = service.\@("from")
    val to = service.\@("to")
    val mappingName = cToPascal(s"${from}_${to}_${impl}")
    val printWriter = new PrintWriter(s"${implSrcDir}/${mappingName}.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${implSrcPackage}\n")
    // imports
    printWriter.println(s"${importPackagesForService(model.xml, service)}")
    // companion object declaration
    printWriter.println(
      s"""
         |object ${mappingName} {
         |  def name = "${mappingName}"
         |}
       """.stripMargin)
    // begin class declaration
    printWriter.println(
      s"""class ${mappingName} @Inject()(${cToCamel(from)}: ActorRef, @Named("${cToCamel(to)}") ${cToCamel(to)}: ActorRef)
         |  extends PersistentActor
         |    with ActorLogging {
         |  implicit val executionContext = context.system.dispatcher
         |  implicit val materializer = ActorMaterializer()
         |
         |  override def persistenceId: String = ${mappingName}.name
         |
         |  // state
         |  var lastEvent: Option[EventEnvelope] = None
         """.stripMargin)

    printWriter.println(s"${indent(receiveRecover(service), 2, true)}\n")
    printWriter.println(s"${indent(receiveCommand(service), 2, true)}\n")
    printWriter.println(s"${indent(updateState(service), 2, true)}\n")

    // end class declaration
    printWriter.println("}")
    printWriter.close()
  }

  private def receiveRecover(service: Node): String = {
    s"""
       |override def receiveRecover: Receive = {
       |  case SnapshotOffer(metadata: SnapshotMetadata, snapshot: EventEnvelope) =>
       |    lastEvent = Some(snapshot)
       |  case _: RecoveryCompleted =>
       |    // TODO: connect to source service and start event subscription.
       |  case x =>
       |    updateState(x)
       |}
     """.stripMargin
      .trim
  }

  private def receiveCommand(service: Node): String = {
    s"""
       |override def receiveCommand: Receive = {
       |  case x: EventEnvelope =>
       |    // TODO: process event
       |    persist(x)(updateState)
       |  case x =>
       |    log.info("unhandled command: {}", x)
       |}
     """.stripMargin
      .trim
  }

  private def updateState(service: Node): String = {
    s"""
       |private def updateState: (Any => Unit) = {
       |  case x: EventEnvelope =>
       |    lastEvent = Some(x)
       |    log.info("unhandled update state: {}", x)
       |}
     """.stripMargin
      .trim
  }
}
