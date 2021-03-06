package com.github.apuex.mapping

import akka._
import akka.stream.scaladsl._
import com.lightbend.lagom.scaladsl.api._
import com.github.apuex.springbootsolution.runtime._
import play.api.libs.json.Json

trait SrcService extends Service {

  def retrieveSrcTable1ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable1Vo]
  def retrieveSrcTable2ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable2Vo]
  def querySrcView1(): ServiceCall[QueryCommand, SrcView1ListVo]
  def querySrcView2(): ServiceCall[QueryCommand, SrcView2ListVo]

  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._
    import ScalapbJson._

    implicit val queryCommandFormat = jsonFormat[QueryCommand]
    implicit val retrieveByRowidFormat = jsonFormat[RetrieveByRowidCmd]
    implicit val srcTable1VoFormat = jsonFormat[SrcTable1Vo]
    implicit val srcTable2VoFormat = jsonFormat[SrcTable2Vo]
    implicit val srcView1VoFormat = jsonFormat[SrcView1Vo]
    implicit val srcView1ListVoFormat = jsonFormat[SrcView1ListVo]
    implicit val srcView2VoFormat = jsonFormat[SrcView2Vo]
    implicit val srcView2ListVoFormat = jsonFormat[SrcView2ListVo]

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
