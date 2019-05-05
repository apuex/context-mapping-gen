package com.apuex.sales.mapping.bc1ToBc2

import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class Bc1ToBc2MappingServiceImpl extends Bc1ToBc2MappingService {
  /**
    * Subscribe from event stream with offset.
    *
    * @see [[akka.persistence.query.Offset]]
    * @see [[akka.persistence.query.TimeBasedUUID]]
    * @param offset timed-uuid specifies start position
    * @return
    */
  def events(offset: Option[String]) = ServiceCall { inputStream =>
    println(s"offset = ${offset}")
    Future.successful(inputStream.map(x => x))
  }

}
