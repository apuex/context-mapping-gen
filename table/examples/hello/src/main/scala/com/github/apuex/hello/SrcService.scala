package com.github.apuex.hello

import akka._
import akka.stream.scaladsl._
import com.github.apuex.springbootsolution.runtime.QueryCommand
import com.lightbend.lagom.scaladsl.api._

case class SrcView1Vo(col1: String, col2: String, col3: String, col4: String)
case class SrcView1ListVo(items: Seq[SrcView1Vo])

case class SrcView2Vo(col1: String, col2: String, col3: String, col4: String)
case class SrcView2ListVo(items: Seq[SrcView2Vo])

case class SrcTable1Vo(col1: String, col2: String, col3: String, col4: String)

trait SrcService extends Service {
  def retrieveSrcTable1ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable1Vo]
  def querySrcView1ByCol1Col2(): ServiceCall[QueryCommand, SrcView1ListVo]
  def querySrcView2ByCol1(): ServiceCall[QueryCommand, SrcView2ListVo]
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._

    named("src")
      .withCalls(
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
