package com.apuex.sales.mapping.bc1ToBc2

import javax.inject._

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
       
trait OrderService extends Service {
  /**
    * Subscribe from event stream with offset.
    *
    * @see [[akka.persistence.query.Offset]]
    * @see [[akka.persistence.query.TimeBasedUUID]]
    * @param offset timed-uuid specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
         
  def retrieve(): ServiceCall[RetrieveOrderCmd, OrderVo]

  
  override final def descriptor: Descriptor = {
    import Service._
  
    named("order")
      .withCalls(
        pathCall("/api/events", events _)
      ).withAutoAcl(true)
  }
       
}
