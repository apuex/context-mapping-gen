package com.apuex.sales.mapping.bc1ToBc2Mapping

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait InventoryService extends Service {
  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  def reduce(): ServiceCall[ReduceStorageCmd, Done]


  override final def descriptor: Descriptor = {
    import Service._
    import ScalapbJson._

    implicit val reduceStorageCmdFormat = jsonFormat[ReduceStorageCmd]

    named("inventory")
      .withCalls(
        pathCall("/api/events?offset", events _),
        pathCall("/api/reduce", reduce _)
      ).withAutoAcl(true)
  }
}
