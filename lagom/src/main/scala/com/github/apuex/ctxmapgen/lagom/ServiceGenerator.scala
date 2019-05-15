package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.lagom.MappingLoader.importPackages
import com.github.apuex.ctxmapgen.lagom.ServiceGenerator._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.collection.mutable
import scala.xml._

object ServiceGenerator {

  case class OperationDescription(name: String, req: String, resp: String)

  def collectService(node: Node, calls: mutable.Map[String, mutable.Set[OperationDescription]]): Unit = {
    node.child.filter(x => x.label == "service")
      .foreach(x => {
        val from = x.\@("from")
        val to = x.\@("to")
        val serviceName = (s"${from}_${to}")
        val operations = calls.getOrElse(serviceName, mutable.Set())
        calls += (serviceName -> operations)
      })
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

  def generateServiceDescriptor(service: (String, mutable.Set[OperationDescription])): String = {
    s"""
       |override final def descriptor: Descriptor = {
       |  import Service._
       |
       |  named("${cToShell(service._1)}")
       |    .withCalls(
       |      pathCall("/api/events?offset", events _)
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
    collectService(xml, serviceCalls)
    collectServiceCalls(xml, serviceCalls)
    serviceCalls.foreach(x => generateService(x))
  }

  def generateService(serviceCalls: (String, mutable.Set[OperationDescription])): Unit = {
    val serviceName = cToPascal(s"${serviceCalls._1}_${service}")
    val jsonSupportName = cToPascal(s"${serviceCalls._1}_json_support")
    new File(apiSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${apiSrcDir}/${serviceName}.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${apiSrcPackage}\n")
    // imports
    printWriter.println(s"${importPackages(xml)}")
    // companion object declaration
    printWriter.println(
      s"""
         |import akka.{Done, NotUsed}
         |import akka.stream.scaladsl.Source
         |import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
       """.stripMargin)
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
         """.stripMargin)

    serviceCalls._2.foreach(m => {
      printWriter.println(s"${indent(generateServiceOperation(serviceCalls._1, m), 2, true)}\n")
    })
    // method descriptor
    printWriter.println(s"${indent(generateServiceDescriptor(serviceCalls), 2, true)}")
    // end class declaration
    printWriter.println("}")
    printWriter.close()
  }
}
