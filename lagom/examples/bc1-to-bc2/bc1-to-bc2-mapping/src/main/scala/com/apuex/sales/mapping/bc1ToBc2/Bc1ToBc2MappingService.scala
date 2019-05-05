package com.apuex.sales.mapping.bc1ToBc2

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait Bc1ToBc2MappingService extends Service {
  /**
    * Subscribe from event stream with offset.
    *
    * @see [[akka.persistence.query.Offset]]
    * @see [[akka.persistence.query.TimeBasedUUID]]
    * @param offset timed-uuid specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._

    named("bc1-to-bc2-mapping")
      .withCalls(
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
