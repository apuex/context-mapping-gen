
package com.apuex.sales.mapping.bc1ToBc2.impl

import akka.actor.Props
import com.apuex.sales.mapping.bc1ToBc2._
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.playjson._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

class OrderInventoryMappingAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new OrderInventoryMappingApp(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new OrderInventoryMappingApp(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[OrderInventoryService])
}

abstract class OrderInventoryMappingApp(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  lazy val mappingConfig = new MappingConfig()
  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[OrderInventoryService](wire[OrderInventoryServiceImpl])
  // Register the JSON serializer registry
  override lazy val optionalJsonSerializerRegistry = Some(new JsonSerializerRegistry {
    import mappingConfig._
    override def serializers: Seq[JsonSerializer[_]] = Seq(
      JsonSerializer(jsonFormat(classOf[RetrieveOrderCmd])),
      JsonSerializer(jsonFormat(classOf[OrderVo])),
      JsonSerializer(jsonFormat(classOf[RetrieveProductCmd])),
      JsonSerializer(jsonFormat(classOf[ProductVo])),
      JsonSerializer(jsonFormat(classOf[ReduceStorageCmd]))
    )
  })

  // Bind the service clients
  lazy val order: OrderService = serviceClient.implement[OrderService]
  lazy val product: ProductService = serviceClient.implement[ProductService]
  lazy val inventory: InventoryService = serviceClient.implement[InventoryService]

  val mapping = actorSystem.actorOf(
    Props(new OrderInventoryMapping(
      mappingConfig,
      order,
      product,
      inventory
    )),
    OrderInventoryMapping.name
  )
}
     
