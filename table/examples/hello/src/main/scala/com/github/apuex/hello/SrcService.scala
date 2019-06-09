package com.github.apuex.hello

import akka._
import akka.stream.scaladsl._
import com.lightbend.lagom.scaladsl.api._

case class RetrieveSrcView1Cmd(col1: String, col2: String)
case class SrcView1Vo(col1: String, col2: String, col3: String, col4: String)
case class RetrieveSrcView2Cmd(col1: String)
case class SrcView2Vo(col1: String, col2: String, col3: String, col4: String)
case class SrcTable1Vo(col1: String, col2: String, col3: String, col4: String)

trait SrcService extends Service {
  def retrieveSrcTable1ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable1Vo]
  def retrieveSrcView1(): ServiceCall[RetrieveSrcView1Cmd, SrcView1Vo]
  def retrieveSrcView2(): ServiceCall[RetrieveSrcView2Cmd, SrcView2Vo]
  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._


    named("src")
      .withCalls(
        pathCall("/api/events?offset", events _)
      ).withAutoAcl(true)
  }
}
