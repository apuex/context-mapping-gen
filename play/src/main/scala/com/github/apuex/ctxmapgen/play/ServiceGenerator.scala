package com.github.apuex.ctxmapgen.play

import java.io.PrintWriter

import MappingLoader.importPackages
import com.github.apuex.springbootsolution.runtime.SymbolConverters.{cToCamel, cToPascal}
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.collection.mutable
import scala.xml._

class ServiceGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  case class OperationDescription(name: String, req: String, resp: String)

  val serviceCalls: mutable.Map[String, mutable.Set[OperationDescription]] = mutable.Map()


  def generate(): Unit = {
    generateRequestUtil()
    collectServiceCalls(xml, serviceCalls)
    serviceCalls.foreach(x => generateService(x))
  }

  private def collectServiceCalls(node: Node, calls: mutable.Map[String, mutable.Set[OperationDescription]]): Unit = {
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

  def generateService(service: (String, mutable.Set[OperationDescription])): Unit = {
    val serviceName = cToPascal(s"${service._1}_service")
    val jsonSupportName = cToPascal(s"${service._1}_json_support")
    val printWriter = new PrintWriter(s"${srcDir}/${serviceName}.scala", "utf-8")
    // package definition
    printWriter.println(s"package ${srcPackage}\n")
    // imports
    printWriter.println(s"${importPackages(xml)}")
    // companion object declaration
    printWriter.println(
      s"""
         |import com.apuex.sales.mapping.bc1ToBc2.${serviceName}._
         |import javax.inject._
         |import play.api.Configuration
         |import play.api.libs.json.Json
         |import play.api.libs.ws._
         |
         |import scala.concurrent.duration._
         |import scala.concurrent._
         |
         |object ${serviceName} {
         |  def name = "${serviceName}"
         |  // TODO: implement case classes.
         |}
       """.stripMargin)
    // begin class declaration
    printWriter.println(
      s"""class ${serviceName} @Inject()(config: Configuration, ws: WSClient, implicit val ec: ExecutionContext)
         |  extends ${jsonSupportName} {
         |  val baseUrl: String = config.underlying.getString("order-service.base-url")
         |  val timeout: Duration = config.underlying.getDuration("order-service.request-timeout").asInstanceOf
         |  val requestUtil = new RequestUtil(baseUrl, timeout, ws)
         |  import requestUtil._
         |  // TODO: implement json format for case classes.
         |
         """.stripMargin)

    service._2.foreach(m => {
      printWriter.println(s"${indent(generateServiceOperation(service._1, m), 2, true)}\n")
    })
    // end class declaration
    printWriter.println("}")
    printWriter.close()
  }

  def generateServiceOperation(service: String, operation: OperationDescription): String = {
    val req = if (operation.req.isEmpty) s"${cToPascal(s"${operation.name}_${service}_cmd")}" else cToPascal(s"${operation.req}_cmd")
    val resp = if (operation.resp.isEmpty) "Unit" else cToPascal(s"${operation.resp}_vo")
    if ("Unit" == resp)
      s"""
         |def ${cToCamel(operation.name)}(cmd: ${req}): ${resp} = {
         |  Await.result(
         |    request("${cToCamel(operation.name)}", "application/json")
         |      .post(Json.toJson(cmd)),
         |    timeout)
         |}
     """.stripMargin
        .trim
    else
      s"""
         |def ${cToCamel(operation.name)}(cmd: ${req}): ${resp} = {
         |  Await.result(
         |    request("${cToCamel(operation.name)}", "application/json")
         |      .post(Json.toJson(cmd))
         |      .map({ response =>
         |        response.json.validate[ProductVo]
         |          .fold(
         |            errors => throw new RuntimeException(errors.toString()),
         |            x => x
         |          )
         |      }),
         |    timeout)
         |}
     """.stripMargin
        .trim
  }

  def generateRequestUtil(): Unit = {
    val printWriter = new PrintWriter(s"${srcDir}/RequestUtil.scala", "utf-8")
    // class declaration
    printWriter.println(
      s"""package ${srcPackage}
         |
         |import play.api.libs.ws.{WSClient, WSRequest}
         |
         |import scala.concurrent.duration.Duration
         |
         |class RequestUtil(val baseUrl: String, timeout: Duration, ws: WSClient) {
         |
         |  def request(uri: String, accept: String): WSRequest = {
         |    ws.url(s"$${baseUrl}/$${uri}")
         |      .addHttpHeaders("Accept" -> accept)
         |      .withFollowRedirects(true)
         |      .withRequestTimeout(timeout)
         |  }
         |}
         """.stripMargin)
    printWriter.close()
  }
}
