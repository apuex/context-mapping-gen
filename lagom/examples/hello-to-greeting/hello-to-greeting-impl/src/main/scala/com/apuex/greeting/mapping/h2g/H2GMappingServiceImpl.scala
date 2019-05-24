package com.apuex.greeting.mapping.h2g

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api._

import scala.concurrent.Future

class H2GMappingServiceImpl(config: MappingConfig) extends H2GMappingService {

  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid or sequence specifies start position
    * @return
    */
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = ServiceCall { is =>
    Future.successful(is.map(x => x))
  }
}
