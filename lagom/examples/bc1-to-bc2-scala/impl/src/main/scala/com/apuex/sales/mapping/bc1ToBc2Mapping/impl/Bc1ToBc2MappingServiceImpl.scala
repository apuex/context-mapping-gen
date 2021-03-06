package com.apuex.sales.mapping.bc1ToBc2Mapping.impl

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.apuex.sales.mapping.bc1ToBc2Mapping.Bc1ToBc2MappingService

import scala.concurrent.Future

class Bc1ToBc2MappingServiceImpl extends Bc1ToBc2MappingService {
  /**
    * Subscribe from event stream with offset.
    *
    * @param offset timed-uuid specifies start position
    * @return
    */
  override def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] =
    ServiceCall { is =>
      Future.successful(is.map(x => x))
    }
}
