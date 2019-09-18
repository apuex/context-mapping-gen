package com.apuex.sales.mapping.bc1ToBc2Mapping

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.stream.scaladsl.Source
import akka.util.Timeout

import scala.concurrent.duration.Duration

class MappingConfig {
  implicit val duration = Duration.apply(30, TimeUnit.SECONDS)
  implicit val timeout = Timeout(duration)
  val snapshotSequenceCount: Long = 1000

  val keepAlive = Source.fromIterator(() => new Iterator[String] {
    override def hasNext: Boolean = true

    override def next(): String = "{}"
  })
    .throttle(1, duration)
    .map(_.toString)
}
