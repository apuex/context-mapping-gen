package com.apuex.sales.mapping.bc1ToBc2Mapping

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.Json

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

    implicit val retrieveOrderCmdFormat = Json.format[RetrieveOrderCmd]
    implicit val orderItemVoFormat = Json.format[OrderItemVo]
    implicit val orderVoFormat = Json.format[OrderVo]

    named("order")
      .withCalls(
        pathCall("/api/retrieve-order", retrieve _),
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
