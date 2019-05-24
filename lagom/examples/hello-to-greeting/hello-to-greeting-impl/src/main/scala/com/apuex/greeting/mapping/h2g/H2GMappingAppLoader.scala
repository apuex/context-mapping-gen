
package com.apuex.greeting.mapping.h2g

import akka.actor.Props
import com.example.leveldb._
import com.github.apuex.events.play.EventEnvelope
import com.lightbend.lagom.scaladsl.client._
import com.lightbend.lagom.scaladsl.devmode._
import com.lightbend.lagom.scaladsl.playjson._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

class H2GMappingAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new H2GMappingApp(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new H2GMappingApp(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[H2GMappingService])
}

abstract class H2GMappingApp(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  lazy val mappingConfig = new MappingConfig()
  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[H2GMappingService](wire[H2GMappingServiceImpl])
  // Register the JSON serializer registry
  override lazy val optionalJsonSerializerRegistry = Some(new JsonSerializerRegistry {

    import mappingConfig._

    override def serializers: Seq[JsonSerializer[_]] = Seq(
      JsonSerializer(jsonFormat[EventEnvelope]),
      JsonSerializer(jsonFormat[TakeSnapshotCommand]),
      JsonSerializer(jsonFormat[ShutdownSystemCommand]),
      JsonSerializer(jsonFormat[SayHelloCommand]),
      JsonSerializer(jsonFormat[SayHelloEvent])
    )
  })

  // Bind the service clients
  lazy val hello: HelloService = serviceClient.implement[HelloService]

  val mapping = actorSystem.actorOf(
    Props(new H2GMapping(
      mappingConfig,
      hello
    )),
    H2GMapping.name
  )
}
     
