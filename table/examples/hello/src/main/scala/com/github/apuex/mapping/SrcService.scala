package com.github.apuex.mapping

import akka._
import akka.stream.scaladsl._
import com.lightbend.lagom.scaladsl.api._
import com.github.apuex.springbootsolution.runtime._
import play.api.libs.json.Json

case class RetrieveByRowidCmd(rowid: String)
case class SrcTable1Vo(rowid: String)
case class SrcTable1ListVo(rowid: String)
case class SrcTable2Vo(rowid: String)
case class SrcTable2ListVo(rowid: String)
case class SrcView1Vo(rowid: String)
case class SrcView1ListVo(rowid: String)
case class SrcView2Vo(rowid: String)
case class SrcView2ListVo(rowid: String)

trait SrcService extends Service {
  def retrieveSrcTable1ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable1Vo]
  def retrieveSrcTable2ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable2Vo]
  def querySrcView1(): ServiceCall[QueryCommand, SrcView1ListVo]
  def querySrcView2(): ServiceCall[QueryCommand, SrcView2ListVo]
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._

    implicit val retrieveByRowidCmdFormat = Json.format[RetrieveByRowidCmd]
    implicit val srcTable1VoFormat = Json.format[SrcTable1Vo]
    implicit val srcTable2VoFormat = Json.format[SrcTable2Vo]
    implicit val queryCommandFormat = Json.format[QueryCommand]
    implicit val srcView1VoFormat = Json.format[SrcView1Vo]
    implicit val srcView1ListVoFormat = Json.format[SrcView1ListVo]
    implicit val srcView2VoFormat = Json.format[SrcView2Vo]
    implicit val srcView2ListVoFormat = Json.format[SrcView2ListVo]

    named("src")
      .withCalls(
        pathCall("/api/retrieve-src-table-1-by-rowid", retrieveSrcTable1ByRowid _),
        pathCall("/api/retrieve-src-table-2-by-rowid", retrieveSrcTable2ByRowid _),
        pathCall("/api/query-src-view-1", querySrcView1 _),
        pathCall("/api/query-src-view-2", querySrcView2 _),
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
