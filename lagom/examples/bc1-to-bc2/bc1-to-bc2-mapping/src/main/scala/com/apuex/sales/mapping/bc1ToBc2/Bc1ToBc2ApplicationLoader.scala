package com.apuex.sales.mapping.bc1ToBc2

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._

class Bc1ToBc2ApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication = new Bc1ToBc2Application(context) {
    override def serviceLocator: ServiceLocator = NoServiceLocator
  }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new Bc1ToBc2Application(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[Bc1ToBc2MappingService])
}

abstract class Bc1ToBc2Application(context: LagomApplicationContext) extends LagomApplication(context) with AhcWSComponents {
  override def lagomServer: LagomServer = serverFor[Bc1ToBc2MappingService](wire[Bc1ToBc2MappingServiceImpl])
}