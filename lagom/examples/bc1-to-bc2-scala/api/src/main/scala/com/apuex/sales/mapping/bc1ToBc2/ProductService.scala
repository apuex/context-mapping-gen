package com.apuex.sales.mapping.bc1ToBc2

import javax.inject._

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
       
trait ProductService extends Service {
  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
         
  def retrieve(): ServiceCall[RetrieveProductCmd, ProductVo]

  
  override final def descriptor: Descriptor = {
    import Service._
  
    named("product")
      .withCalls(
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
       
}
