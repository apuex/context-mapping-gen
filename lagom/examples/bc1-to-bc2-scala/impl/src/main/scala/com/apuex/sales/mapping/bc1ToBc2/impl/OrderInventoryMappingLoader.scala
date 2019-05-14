package com.apuex.sales.mapping.bc1ToBc2.impl

import akka.actor.Props
import com.apuex.sales.mapping.bc1ToBc2._
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc._
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._
import scala.collection.immutable.Seq

class OrderInventoryMappingLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new OrderInventoryMappingApplication(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new OrderInventoryMappingApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[OrderInventoryService])
}

abstract class OrderInventoryMappingApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  lazy val mappingConfig = new MappingConfig()
  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[OrderInventoryService](wire[OrderInventoryServiceImpl])
  // Register the JSON serializer registry
  override lazy val optionalJsonSerializerRegistry = Some(new JsonSerializerRegistry {
    import mappingConfig._
    override def serializers: Seq[JsonSerializer[_]] = Seq(
      JsonSerializer[ReduceStorageCmd](jsonFormat(classOf[ReduceStorageCmd])),
      JsonSerializer[RetrieveOrderCmd](jsonFormat(classOf[RetrieveOrderCmd])),
      JsonSerializer[PayOrderEvt](jsonFormat(classOf[PayOrderEvt])),
      JsonSerializer[OrderItemVo](jsonFormat(classOf[OrderItemVo])),
      JsonSerializer[OrderVo](jsonFormat(classOf[OrderVo])),
      JsonSerializer[RetrieveProductCmd](jsonFormat(classOf[RetrieveProductCmd])),
      JsonSerializer[ProductVo](jsonFormat(classOf[ProductVo]))
    )
  })

  // Bind the service clients
  lazy val orderService: OrderService = serviceClient.implement[OrderService]
  lazy val productService: ProductService = serviceClient.implement[ProductService]
  lazy val inventoryService: InventoryService = serviceClient.implement[InventoryService]

  val mapping = actorSystem.actorOf(
    Props(new OrderInventoryMapping(
      mappingConfig,
      orderService,
      productService,
      inventoryService
    )),
    OrderInventoryMapping.name
  )
  println(s"mapping actor => ${mapping.path}")
}
