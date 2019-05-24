package com.apuex.greeting.mapping.h2g

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api._

trait HelloService extends Service {

  def echo(msg: String): ServiceCall[NotUsed, String]

  def sayHello(to: String): ServiceCall[NotUsed, String]

  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid or sequence specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("hello")
      .withCalls(
        pathCall("/api/say-hello?to", sayHello _),
        pathCall("/api/echo/:msg", echo _),
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
