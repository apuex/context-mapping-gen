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
         |
         |trait SrcService extends Service {
         |  def retrieveSrcTable1ByRowid: ServiceCall[RetrieveByRowidCmd, SrcTable1Vo]
         |  def querySrcView1: ServiceCall[QueryCommand, SrcView1ListVo]
         |  def querySrcView2: ServiceCall[QueryCommand, SrcView2ListVo]
         |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
         |
         |  override def descriptor: Descriptor = {
         |    import Service._
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
         |
         |trait DestService extends Service {
         |  def createDestTable1: ServiceCall[CreateDestTable1Cmd, NotUsed]
         |  def updateDestTable1: ServiceCall[UpdateDestTable1Cmd, NotUsed]
         |  def deleteDestTable1: ServiceCall[DeleteDestTable1Cmd, NotUsed]
         |  def createDestTable2: ServiceCall[CreateDestTable2Cmd, NotUsed]
         |  def updateDestTable2: ServiceCall[UpdateDestTable2Cmd, NotUsed]
         |  def deleteDestTable2: ServiceCall[DeleteDestTable2Cmd, NotUsed]
         |  def createDestTable5: ServiceCall[CreateDestTable5Cmd, NotUsed]
         |  def updateDestTable5: ServiceCall[UpdateDestTable5Cmd, NotUsed]
         |  def deleteDestTable5: ServiceCall[DeleteDestTable5Cmd, NotUsed]
         |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
         |
         |  override def descriptor: Descriptor = {
         |    import Service._
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
         |
       """.stripMargin.trim)
  }
}
