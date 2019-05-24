package com.apuex.greeting.mapping.h2g

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api._

trait H2GMappingService extends Service {
  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid or sequence specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("hello-to-greeting-mapping")
      .withCalls(
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
