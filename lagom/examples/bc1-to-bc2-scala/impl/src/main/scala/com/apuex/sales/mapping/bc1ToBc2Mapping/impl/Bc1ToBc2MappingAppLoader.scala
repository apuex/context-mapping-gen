
package com.apuex.sales.mapping.bc1ToBc2Mapping.impl

import akka.actor.Props
import com.apuex.sales.mapping.bc1ToBc2Mapping._
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.playjson._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

class Bc1ToBc2MappingAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new Bc1ToBc2MappingApp(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new Bc1ToBc2MappingApp(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[Bc1ToBc2MappingService])
}

abstract class Bc1ToBc2MappingApp(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  lazy val mappingConfig = new MappingConfig()
  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[Bc1ToBc2MappingService](wire[Bc1ToBc2MappingServiceImpl])
  // Register the JSON serializer registry
  override lazy val optionalJsonSerializerRegistry = Some(new JsonSerializerRegistry {

    import mappingConfig._

    override def serializers: Seq[JsonSerializer[_]] = Seq(
      JsonSerializer(jsonFormat[RetrieveOrderCmd]),
      JsonSerializer(jsonFormat[OrderVo]),
      JsonSerializer(jsonFormat[RetrieveProductCmd]),
      JsonSerializer(jsonFormat[ProductVo]),
      JsonSerializer(jsonFormat[ReduceStorageCmd])
    )
  })

  // Bind the service clients
  lazy val order: OrderService = serviceClient.implement[OrderService]
  lazy val product: ProductService = serviceClient.implement[ProductService]
  lazy val inventory: InventoryService = serviceClient.implement[InventoryService]

  val mapping = actorSystem.actorOf(
    Props(new Bc1ToBc2Mapping(
      mappingConfig,
      order,
      product,
      inventory
    )),
    Bc1ToBc2Mapping.name
  )
}
     
