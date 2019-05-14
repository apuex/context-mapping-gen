package com.apuex.sales.mapping.bc1ToBc2.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.apuex.sales.mapping.bc1ToBc2.OrderInventoryService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class OrderInventoryServiceImpl extends OrderInventoryService {
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
