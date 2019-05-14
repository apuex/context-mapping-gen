package com.apuex.sales.mapping.bc1ToBc2.impl

import akka.actor.Props
import com.apuex.sales.mapping.bc1ToBc2._
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc._

class OrderInventoryMappingLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new OrderInventoryMappingApplication(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new OrderInventoryMappingApplication(context) with LagomDevModeComponents

}

abstract class OrderInventoryMappingApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[OrderInventoryService](wire[OrderInventoryServiceImpl])

  // Bind the service clients
  lazy val orderService: OrderService = serviceClient.implement[OrderService]
  lazy val productService: ProductService = serviceClient.implement[ProductService]
  lazy val inventoryService: InventoryService = serviceClient.implement[InventoryService]

  val mappingConfig = new MappingConfig()
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
