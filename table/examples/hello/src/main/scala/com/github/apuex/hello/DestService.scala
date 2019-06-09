package com.github.apuex.hello

import akka._
import akka.stream.scaladsl._
import com.lightbend.lagom.scaladsl.api._

case class CreateDestTable1Cmd(col1: String, col2: String, col3: String, col4: String)
case class UpdateDestTable1Cmd(col1: String, col2: String, col3: String, col4: String)
case class DeleteDestTable1Cmd(col1: String, col2: String)
case class DestTable1Vo(col1: String, col2: String, col3: String, col4: String)

case class CreateDestTable2Cmd(col1: String, col2: String, col3: String, col4: String)
case class UpdateDestTable2Cmd(col1: String, col2: String, col3: String, col4: String)
case class DeleteDestTable2Cmd(col1: String, col2: String)
case class DestTable2Vo(col1: String, col2: String, col3: String, col4: String)

case class CreateDestTable5Cmd(col1: String, col2: String, col3: String)
case class UpdateDestTable5Cmd(col1: String, col2: String, col3: String)
case class DeleteDestTable5Cmd(col1: String, col2: String)
case class DestTable5Vo(col1: String, col2: String, col3: String)

trait DestService extends Service {
  def createDestTable1(): ServiceCall[CreateDestTable1Cmd, NotUsed]
  def updateDestTable1(): ServiceCall[UpdateDestTable1Cmd, NotUsed]
  def deleteDestTable1(): ServiceCall[DeleteDestTable1Cmd, NotUsed]
  def createDestTable2(): ServiceCall[CreateDestTable2Cmd, NotUsed]
  def updateDestTable2(): ServiceCall[UpdateDestTable2Cmd, NotUsed]
  def deleteDestTable2(): ServiceCall[DeleteDestTable2Cmd, NotUsed]
  def createDestTable5(): ServiceCall[CreateDestTable5Cmd, NotUsed]
  def updateDestTable5(): ServiceCall[UpdateDestTable5Cmd, NotUsed]
  def deleteDestTable5(): ServiceCall[DeleteDestTable5Cmd, NotUsed]
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
  override def descriptor: Descriptor = {
    import Service._

    named("dest")
      .withCalls(
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
