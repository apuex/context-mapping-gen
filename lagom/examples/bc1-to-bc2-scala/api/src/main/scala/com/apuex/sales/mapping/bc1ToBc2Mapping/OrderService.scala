package com.apuex.sales.mapping.bc1ToBc2Mapping

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait OrderService extends Service {
  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  def retrieve(): ServiceCall[RetrieveOrderCmd, OrderVo]


  override final def descriptor: Descriptor = {
    import Service._
    import ScalapbJson._
    implicit val RetrieveOrderCmdFormat = jsonFormat[RetrieveOrderCmd]
    implicit val OrderVoFormat = jsonFormat[OrderVo]

    named("order")
      .withCalls(
        pathCall("/api/retrieve", retrieve _),
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
