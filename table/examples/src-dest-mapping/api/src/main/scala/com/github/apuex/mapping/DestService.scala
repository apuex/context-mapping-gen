package com.github.apuex.mapping

import akka._
import akka.stream.scaladsl._
import com.lightbend.lagom.scaladsl.api._
import play.api.libs.json.Json

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
  def createDestTable3(): ServiceCall[CreateDestTable3Cmd, NotUsed]
  def updateDestTable3(): ServiceCall[UpdateDestTable3Cmd, NotUsed]
  def deleteDestTable3(): ServiceCall[DeleteDestTable3Cmd, NotUsed]
  def createDestTable4(): ServiceCall[CreateDestTable4Cmd, NotUsed]
  def updateDestTable4(): ServiceCall[UpdateDestTable4Cmd, NotUsed]
  def deleteDestTable4(): ServiceCall[DeleteDestTable4Cmd, NotUsed]

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._
    import ScalapbJson._

    implicit val createDestTable1CmdFormat = jsonFormat[CreateDestTable1Cmd]
    implicit val updateDestTable1CmdFormat = jsonFormat[UpdateDestTable1Cmd]
    implicit val deleteDestTable1CmdFormat = jsonFormat[DeleteDestTable1Cmd]
    implicit val createDestTable2CmdFormat = jsonFormat[CreateDestTable2Cmd]
    implicit val updateDestTable2CmdFormat = jsonFormat[UpdateDestTable2Cmd]
    implicit val deleteDestTable2CmdFormat = jsonFormat[DeleteDestTable2Cmd]
    implicit val createDestTable5CmdFormat = jsonFormat[CreateDestTable5Cmd]
    implicit val updateDestTable5CmdFormat = jsonFormat[UpdateDestTable5Cmd]
    implicit val deleteDestTable5CmdFormat = jsonFormat[DeleteDestTable5Cmd]
    implicit val createDestTable3CmdFormat = jsonFormat[CreateDestTable3Cmd]
    implicit val updateDestTable3CmdFormat = jsonFormat[UpdateDestTable3Cmd]
    implicit val deleteDestTable3CmdFormat = jsonFormat[DeleteDestTable3Cmd]
    implicit val createDestTable4CmdFormat = jsonFormat[CreateDestTable4Cmd]
    implicit val updateDestTable4CmdFormat = jsonFormat[UpdateDestTable4Cmd]
    implicit val deleteDestTable4CmdFormat = jsonFormat[DeleteDestTable4Cmd]

    named("dest")
      .withCalls(
        pathCall("/api/create-dest-table-1", createDestTable1 _),
        pathCall("/api/update-dest-table-1", updateDestTable1 _),
        pathCall("/api/delete-dest-table-1", deleteDestTable1 _),
        pathCall("/api/create-dest-table-2", createDestTable2 _),
        pathCall("/api/update-dest-table-2", updateDestTable2 _),
        pathCall("/api/delete-dest-table-2", deleteDestTable2 _),
        pathCall("/api/create-dest-table-5", createDestTable5 _),
        pathCall("/api/update-dest-table-5", updateDestTable5 _),
        pathCall("/api/delete-dest-table-5", deleteDestTable5 _),
        pathCall("/api/create-dest-table-3", createDestTable3 _),
        pathCall("/api/update-dest-table-3", updateDestTable3 _),
        pathCall("/api/delete-dest-table-3", deleteDestTable3 _),
        pathCall("/api/create-dest-table-4", createDestTable4 _),
        pathCall("/api/update-dest-table-4", updateDestTable4 _),
        pathCall("/api/delete-dest-table-4", deleteDestTable4 _),
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
