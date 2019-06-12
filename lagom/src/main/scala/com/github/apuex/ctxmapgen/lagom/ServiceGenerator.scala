package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.lagom.ServiceGenerator._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.collection.mutable
import scala.xml._

object ServiceGenerator {

  case class OperationDescription(name: String, req: String, resp: String)

  def collectService(loader: MappingLoader, calls: mutable.Map[String, mutable.Set[OperationDescription]]): Unit = {
    val serviceName = (s"${loader.modelName}")
    val operations = calls.getOrElse(serviceName, mutable.Set())
    calls += (serviceName -> operations)
  }

  def collectServiceCalls(node: Node, calls: mutable.Map[String, mutable.Set[OperationDescription]]): Unit = {
    node.child.filter(x => x.label == "service-call")
      .foreach(x => collectServiceCall(x, calls))
    node.child.foreach(x => collectServiceCalls(x, calls))
  }

  private def collectServiceCall(call: Node, calls: mutable.Map[String, mutable.Set[OperationDescription]]): Unit = {
    val serviceName = call.\@("to")
    val operationName = call.\@("operation")
    val requestType = call.\@("request-type")
    val responseType = call.\@("response-type")
    val operations = calls.getOrElse(serviceName, mutable.Set())
    operations += OperationDescription(
      operationName,
      requestType,
      responseType
    )
    calls += (serviceName -> operations)
  }


  def generateServiceOperation(service: String, operation: OperationDescription): String = {
    val req = if (operation.req.isEmpty) s"${cToPascal(s"${operation.name}_${service}_cmd")}" else cToPascal(s"${operation.req}_cmd")
    val resp = if (operation.resp.isEmpty) "Done" else cToPascal(s"${operation.resp}_vo")
    s"""
       |def ${cToCamel(operation.name)}(): ServiceCall[${req}, ${resp}]
     """.stripMargin
      .trim
  }

  def generateServiceCall(service: String, operation: OperationDescription): String = {
    val req = if (operation.req.isEmpty) s"${cToPascal(s"${operation.name}_${service}_cmd")}" else cToPascal(s"${operation.req}_cmd")
    val resp = if (operation.resp.isEmpty) "Done" else cToPascal(s"${operation.resp}_vo")
    s"""
       |pathCall("/api/${cToShell(operation.name)}", ${cToCamel(operation.name)} _)
     """.stripMargin
      .trim
  }

  def generateServiceDescriptor(service: (String, mutable.Set[OperationDescription])): String = {
    val pathCalls: String = (
      service._2
        .map(x => generateServiceCall(service._1, x)) ++
        Seq(s"""pathCall("/api/events?offset", events _)""")
      )
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")
    s"""
       |override final def descriptor: Descriptor = {
       |  import Service._
       |  import ScalapbJson._
       |
       |  named("${cToShell(service._1)}")
       |    .withCalls(
       |      ${indent(pathCalls, 6)}
       |    ).withAutoAcl(true)
       |}
     """.stripMargin
      .trim
  }
}

class ServiceGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    val serviceCalls: mutable.Map[String, mutable.Set[OperationDescription]] = mutable.Map()
    collectService(mappingLoader, serviceCalls)
    serviceCalls.foreach(x => generateServiceImpl(x))
    collectServiceCalls(xml, serviceCalls)
    serviceCalls.foreach(x => generateService(x))
  }

  def generateService(serviceCalls: (String, mutable.Set[OperationDescription])): Unit = {
    val serviceName = cToPascal(s"${serviceCalls._1}_${service}")
    new File(apiSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${apiSrcDir}/${serviceName}.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${apiSrcPackage}\n")
    // companion object declaration
    printWriter.println(
      s"""
         |import akka.{Done, NotUsed}
         |import akka.stream.scaladsl.Source
         |import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
       """.stripMargin.trim)
    printWriter.println()
    // begin class declaration
    printWriter.println(
      s"""trait ${serviceName} extends Service {
         |  /**
         |    * Subscribe from event stream with offset.
         |    *
         |    * @param offset timed-uuid specifies start position
         |    * @return
         |    */
         |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
         """.stripMargin.trim)

    serviceCalls._2.foreach(m => {
      printWriter.println(s"\n${indent(generateServiceOperation(serviceCalls._1, m), 2, true)}\n")
    })
    // method descriptor
    printWriter.println(s"\n${indent(generateServiceDescriptor(serviceCalls), 2, true)}")
    // end class declaration
    printWriter.println("}")
    printWriter.close()
  }

  def generateServiceImpl(serviceCalls: (String, mutable.Set[OperationDescription])): Unit = {
    val serviceName = cToPascal(s"${serviceCalls._1}_${service}")
    val serviceImplName = cToPascal(s"${serviceName}_${impl}")
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implSrcDir}/${serviceImplName}.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${implSrcPackage}\n")
    // companion object declaration
    printWriter.println(
      s"""
         |import akka.{Done, NotUsed}
         |import akka.stream.scaladsl.Source
         |import com.lightbend.lagom.scaladsl.api.ServiceCall
         |import ${apiSrcPackage}.${serviceName}
         |
         |import scala.concurrent.Future
       """.stripMargin.trim)
    printWriter.println()
    // begin class declaration
    printWriter.println(
      s"""class ${serviceImplName} extends ${serviceName} {
         |  /**
         |    * Subscribe from event stream with offset.
         |    *
         |    * @param offset timed-uuid specifies start position
         |    * @return
         |    */
         |  override def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] =
         |    ServiceCall { is =>
         |      Future.successful(is.map(x => x))
         |    }
         """.stripMargin.trim)

    // end class declaration
    printWriter.println("}")
    printWriter.close()
  }
}
