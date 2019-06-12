package com.github.apuex.ctxmapgen.table

import com.github.apuex.ctxmapgen.table.ServiceClientGenerator._
import org.scalatest.{FlatSpec, Matchers}

class ServiceClientGeneratorSpec extends FlatSpec with Matchers {

  val mappingXml =
    <table-mappings from="src"
                    to="dest"
                    package="com.github.apuex.mapping"
                    version="1.0.0"
                    maintainer="xtwxy@hotmail.com">
      <src-table name="src_table_1">
        <!--
          filter-key columns, or rowid
        -->
        <filter-key>
          <column name="col_1"/>
          <column name="col_2"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1"/>
            <column name="col_2"/>
          </filter-key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <filter-key>
            <column name="col_1"/>
            <column name="col_2"/>
          </filter-key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <filter-key>
              <column name="col_1"/>
              <column name="col_2"/>
            </filter-key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <filter-key>
            <column name="col_1"/>
          </filter-key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <filter-key>
              <column name="col_1"/>
            </filter-key>
          </dest-table>
        </view>
      </src-table>
    </table-mappings>

  val m = ServiceClientGenerator(MappingLoader(mappingXml))

  import m._

  "A ServiceClientGenerator" should "generate source service client" in {
    generateSrcServiceClient() should be(
      s"""
         |package com.github.apuex.mapping
         |
         |import akka._
         |import akka.stream.scaladsl._
         |import com.lightbend.lagom.scaladsl.api._
         |import com.github.apuex.springbootsolution.runtime._
         |import play.api.libs.json.Json
         |
         |trait SrcService extends Service {
         |
         |  def retrieveSrcTable1ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable1Vo]
         |  def querySrcView1(): ServiceCall[QueryCommand, SrcView1ListVo]
         |  def querySrcView2(): ServiceCall[QueryCommand, SrcView2ListVo]
         |
         |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
         |
         |  override def descriptor: Descriptor = {
         |    import Service._
         |
         |    implicit val queryCommandFormat = Json.format[QueryCommand]
         |    implicit val retrieveByRowidFormat = Json.format[RetrieveByRowidCmd]
         |    implicit val srcTable1VoFormat = Json.format[SrcTable1Vo]
         |    implicit val srcView1VoFormat = Json.format[SrcView1Vo]
         |    implicit val srcView1ListVoFormat = Json.format[SrcView1ListVo]
         |    implicit val srcView2VoFormat = Json.format[SrcView2Vo]
         |    implicit val srcView2ListVoFormat = Json.format[SrcView2ListVo]
         |
         |    named("src")
         |      .withCalls(
         |        pathCall("/api/retrieve-src-table-1-by-rowid", retrieveSrcTable1ByRowid _),
         |        pathCall("/api/query-src-view-1", querySrcView1 _),
         |        pathCall("/api/query-src-view-2", querySrcView2 _),
         |        pathCall("/api/events?offset", events _)
         |      ).withAutoAcl(true)
         |  }
         |}
       """.stripMargin.trim)
  }

  it should "generate destination service client" in {
    generateDestServiceClient() should be(
      s"""
         |package com.github.apuex.mapping
         |
         |import akka._
         |import akka.stream.scaladsl._
         |import com.lightbend.lagom.scaladsl.api._
         |import play.api.libs.json.Json
         |
         |trait DestService extends Service {
         |
         |  def createDestTable1(): ServiceCall[CreateDestTable1Cmd, NotUsed]
         |  def updateDestTable1(): ServiceCall[UpdateDestTable1Cmd, NotUsed]
         |  def deleteDestTable1(): ServiceCall[DeleteDestTable1Cmd, NotUsed]
         |  def createDestTable2(): ServiceCall[CreateDestTable2Cmd, NotUsed]
         |  def updateDestTable2(): ServiceCall[UpdateDestTable2Cmd, NotUsed]
         |  def deleteDestTable2(): ServiceCall[DeleteDestTable2Cmd, NotUsed]
         |  def createDestTable5(): ServiceCall[CreateDestTable5Cmd, NotUsed]
         |  def updateDestTable5(): ServiceCall[UpdateDestTable5Cmd, NotUsed]
         |  def deleteDestTable5(): ServiceCall[DeleteDestTable5Cmd, NotUsed]
         |
         |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
         |
         |  override def descriptor: Descriptor = {
         |    import Service._
         |
         |    implicit val createDestTable1CmdFormat = Json.format[CreateDestTable1Cmd]
         |    implicit val updateDestTable1CmdFormat = Json.format[UpdateDestTable1Cmd]
         |    implicit val deleteDestTable1CmdFormat = Json.format[DeleteDestTable1Cmd]
         |    implicit val createDestTable2CmdFormat = Json.format[CreateDestTable2Cmd]
         |    implicit val updateDestTable2CmdFormat = Json.format[UpdateDestTable2Cmd]
         |    implicit val deleteDestTable2CmdFormat = Json.format[DeleteDestTable2Cmd]
         |    implicit val createDestTable5CmdFormat = Json.format[CreateDestTable5Cmd]
         |    implicit val updateDestTable5CmdFormat = Json.format[UpdateDestTable5Cmd]
         |    implicit val deleteDestTable5CmdFormat = Json.format[DeleteDestTable5Cmd]
         |
         |    named("dest")
         |      .withCalls(
         |        pathCall("/api/create-dest-table-1", createDestTable1 _),
         |        pathCall("/api/update-dest-table-1", updateDestTable1 _),
         |        pathCall("/api/delete-dest-table-1", deleteDestTable1 _),
         |        pathCall("/api/create-dest-table-2", createDestTable2 _),
         |        pathCall("/api/update-dest-table-2", updateDestTable2 _),
         |        pathCall("/api/delete-dest-table-2", deleteDestTable2 _),
         |        pathCall("/api/create-dest-table-5", createDestTable5 _),
         |        pathCall("/api/update-dest-table-5", updateDestTable5 _),
         |        pathCall("/api/delete-dest-table-5", deleteDestTable5 _),
         |        pathCall("/api/events?offset", events _)
         |      ).withAutoAcl(true)
         |  }
         |}
       """.stripMargin.trim)
  }

  it should "generate source service calls" in {
    srcCalls() should be(
      s"""
         |def retrieveSrcTable1ByRowid(): ServiceCall[RetrieveByRowidCmd, SrcTable1Vo]
         |def querySrcView1(): ServiceCall[QueryCommand, SrcView1ListVo]
         |def querySrcView2(): ServiceCall[QueryCommand, SrcView2ListVo]
       """.stripMargin.trim
    )
  }

  it should "generate source service call descriptors" in {
    srcCallDescs() should be(
      s"""
         |pathCall("/api/retrieve-src-table-1-by-rowid", retrieveSrcTable1ByRowid _),
         |pathCall("/api/query-src-view-1", querySrcView1 _),
         |pathCall("/api/query-src-view-2", querySrcView2 _),
       """.stripMargin.trim
    )
  }

  it should "generate destination service calls" in {
    destCalls() should be(
      s"""
         |def createDestTable1(): ServiceCall[CreateDestTable1Cmd, NotUsed]
         |def updateDestTable1(): ServiceCall[UpdateDestTable1Cmd, NotUsed]
         |def deleteDestTable1(): ServiceCall[DeleteDestTable1Cmd, NotUsed]
         |def createDestTable2(): ServiceCall[CreateDestTable2Cmd, NotUsed]
         |def updateDestTable2(): ServiceCall[UpdateDestTable2Cmd, NotUsed]
         |def deleteDestTable2(): ServiceCall[DeleteDestTable2Cmd, NotUsed]
         |def createDestTable5(): ServiceCall[CreateDestTable5Cmd, NotUsed]
         |def updateDestTable5(): ServiceCall[UpdateDestTable5Cmd, NotUsed]
         |def deleteDestTable5(): ServiceCall[DeleteDestTable5Cmd, NotUsed]
       """.stripMargin.trim
    )
  }

  it should "generate destination service call descriptors" in {
    destCallDescs() should be(
      s"""
         |pathCall("/api/create-dest-table-1", createDestTable1 _),
         |pathCall("/api/update-dest-table-1", updateDestTable1 _),
         |pathCall("/api/delete-dest-table-1", deleteDestTable1 _),
         |pathCall("/api/create-dest-table-2", createDestTable2 _),
         |pathCall("/api/update-dest-table-2", updateDestTable2 _),
         |pathCall("/api/delete-dest-table-2", deleteDestTable2 _),
         |pathCall("/api/create-dest-table-5", createDestTable5 _),
         |pathCall("/api/update-dest-table-5", updateDestTable5 _),
         |pathCall("/api/delete-dest-table-5", deleteDestTable5 _),
       """.stripMargin.trim
    )
  }

  it should "generate retrieve by rowid" in {
    retrieveByRowid("table_view") should be(
      "def retrieveTableViewByRowid(): ServiceCall[RetrieveByRowidCmd, TableViewVo]"
    )
  }

  it should "generate create" in {
    create("table_view") should be(
      "def createTableView(): ServiceCall[CreateTableViewCmd, NotUsed]"
    )
  }

  it should "generate retrieve" in {
    retrieve("table_view") should be(
      "def retrieveTableView(): ServiceCall[RetrieveTableViewCmd, TableViewVo]"
    )
  }

  it should "generate query" in {
    query("table_view") should be(
      "def queryTableView(): ServiceCall[QueryCommand, TableViewListVo]"
    )
  }

  it should "generate update" in {
    update("table_view") should be(
      "def updateTableView(): ServiceCall[UpdateTableViewCmd, NotUsed]"
    )
  }

  it should "generate delete" in {
    delete("table_view") should be(
      "def deleteTableView(): ServiceCall[DeleteTableViewCmd, NotUsed]"
    )
  }
}
