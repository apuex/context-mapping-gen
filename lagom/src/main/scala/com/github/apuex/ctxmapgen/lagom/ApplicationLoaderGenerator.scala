package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.lagom.ServiceGenerator.{OperationDescription, collectServiceCalls}
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.collection.mutable

class ApplicationLoaderGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  val mappingName = (s"${modelName}")
  val serviceName = (s"${mappingName}_${service}")
  val serviceImplName = (s"${serviceName}_${impl}")
  val appName = (s"${mappingName}_${app}")
  val loaderName = (s"${appName}_${loader}")

  val serviceCalls: mutable.Map[String, mutable.Set[OperationDescription]] = mutable.Map()
  collectServiceCalls(xml, serviceCalls)

  val appLoader =
    s"""
       |package ${implSrcPackage}
       |
       |import akka.actor.Props
       |import ${apiSrcPackage}._
       |import com.lightbend.lagom.scaladsl.client._
       |import com.lightbend.lagom.scaladsl.devmode._
       |import com.lightbend.lagom.scaladsl.playjson._
       |import com.lightbend.lagom.scaladsl.server._
       |import com.softwaremill.macwire._
       |import play.api.libs.ws.ahc.AhcWSComponents
       |
       |import scala.collection.immutable.Seq
       |
       |class ${cToPascal(loaderName)} extends LagomApplicationLoader {
       |
       |  override def load(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(appName)}(context) with ConfigurationServiceLocatorComponents
       |
       |  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
       |    new ${cToPascal(appName)}(context) with LagomDevModeComponents
       |
       |  override def describeService = Some(readDescriptor[${cToPascal(serviceName)}])
       |}
       |
       |abstract class ${cToPascal(appName)}(context: LagomApplicationContext)
       |  extends LagomApplication(context)
       |    with AhcWSComponents {
       |
       |  lazy val mappingConfig = new MappingConfig()
       |  // Bind the service that this server provides
       |  override lazy val lagomServer: LagomServer = serverFor[${cToPascal(serviceName)}](wire[${cToPascal(serviceImplName)}])
       |  // Register the JSON serializer registry
       |  override lazy val optionalJsonSerializerRegistry = Some(new JsonSerializerRegistry {
       |
       |    import mappingConfig._
       |
       |    override def serializers: Seq[JsonSerializer[_]] = Seq(
       |      ${indent(generateJsonSerializers, 6)}
       |    )
       |  })
       |
       |  // Bind the service clients
       |  ${indent(generateServiceClientDefs, 2)}
       |
       |  val mapping = actorSystem.actorOf(
       |    Props(new ${cToPascal(mappingName)}(
       |      ${indent(generateMappingConstructorParam, 6)}
       |    )),
       |    ${cToPascal(mappingName)}.name
       |  )
       |}
     """.stripMargin

  private def generateServiceClientDefs = serviceCalls
    .map(x => s"lazy val ${cToCamel(x._1)}: ${cToPascal(s"${x._1}_${service}")} = serviceClient.implement[${cToPascal(s"${x._1}_${service}")}]")
    .foldLeft("")((l, r) => s"${l}${r}\n")

  private def generateMappingConstructorParam = serviceCalls
    .map(x => cToCamel(x._1))
    .foldLeft("mappingConfig")((l, r) => s"${l},\n${r}")

  private def generateJsonSerializers = serviceCalls
    .flatMap(x => x._2.map(d => collectMessage(x._1, d)))
    .flatMap(x => x)
    .map(x => s"JsonSerializer(jsonFormat(classOf[${cToPascal(x)}]))")
    .reduce((l, r) => s"${l},\n${r}")

  private def collectMessage(service: String, operation: OperationDescription): Seq[String] = {
    Seq(
      if (operation.req.isEmpty) s"${cToPascal(s"${operation.name}_${service}_cmd")}" else cToPascal(s"${operation.req}_cmd"),
      if (operation.resp.isEmpty) "Done" else cToPascal(s"${operation.resp}_vo")
    ).filter(!_.equals("Done"))
  }

  def generate(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implSrcDir}/${cToPascal(loaderName)}.scala", "utf-8")
    printWriter.println(appLoader)
    printWriter.close()
  }
}
