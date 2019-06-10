package com.github.apuex.ctxmapgen.table

import com.github.apuex.ctxmapgen.table.TableMappingGenerator._
import org.scalatest.{FlatSpec, Matchers}

class TableMappingGeneratorSpec extends FlatSpec with Matchers {

  val mappingXml =
    <table-mappings from="src"
                    to="dest"
                    package="com.github.apuex.mapping"
                    version="1.0.0"
                    maintainer="xtwxy@hotmail.com">
    </table-mappings>

  val m = TableMappingGenerator(MappingLoader(mappingXml))

  import m._

  "A TableMappingGenerator" should "generate table mapping from rowid" in {
    val table =
      <src-table name="src_table_1">
        <!--
          filter-key columns, or rowid
        -->
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <filter-key>
              <column name="col_1" type="string"/>
              <column name="col_2" type="long"/>
            </filter-key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <filter-key>
            <column name="col_1" type="string"/>
          </filter-key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <filter-key>
              <column name="col_1" type="string"/>
            </filter-key>
          </dest-table>
        </view>
      </src-table>

    val destTables = destTableNames(table)
    destTables should be(Seq("dest_table_1", "dest_table_2", "dest_table_5"))
    deletes(destTables) should be(
      s"""
         |case x: DeleteDestTable1Cmd =>
         |  dest.deleteDestTable1().invoke(x)
         |case x: DeleteDestTable2Cmd =>
         |  dest.deleteDestTable2().invoke(x)
         |case x: DeleteDestTable5Cmd =>
         |  dest.deleteDestTable5().invoke(x)
       """.stripMargin.trim)

    val tableMapping = generateTableMapping(table)
    tableMapping._1 should be("src_table_1")
    tableMapping._2 should be(
      s"""
         |package com.github.apuex.mapping
         |
         |import scala.concurrent.ExecutionContext
         |
         |class SrcTable1Mapping (
         |    src: SrcService,
         |    dest: DestService,
         |    addDelete: (String, String, Any) => Unit,
         |    getDeletes: (String, String) => Seq[Any],
         |    implicit val ec: ExecutionContext
         |  ) extends TableMapping {
         |
         |  override def create(tableName: String, rowid: String): Unit = {
         |    src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
         |      .map(t => {
         |        addDelete(tableName, rowid, DeleteDestTable1Cmd(t.col1, t.col2))
         |        dest.createDestTable1().invoke(CreateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |        src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |          .map(v => {
         |            addDelete(tableName, rowid, DeleteDestTable2Cmd(v.col1, v.col2))
         |            dest.createDestTable2().invoke(CreateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |          })
         |        src.querySrcView2ByCol1().invoke(querySrcView2ByCol1Cmd(t.col1))
         |          .map(v => {
         |            addDelete(tableName, rowid, DeleteDestTable5Cmd(v.col1))
         |            dest.createDestTable5().invoke(CreateDestTable5Cmd(v.col1, v.col2, v.col3))
         |          })
         |      })
         |  }
         |
         |  override def update(tableName: String, rowid: String): Unit = {
         |    src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
         |      .map(t => {
         |        dest.updateDestTable1().invoke(UpdateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |        src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |          .map(v => {
         |            dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |          })
         |        src.querySrcView2ByCol1().invoke(querySrcView2ByCol1Cmd(t.col1))
         |          .map(v => {
         |            dest.updateDestTable5().invoke(UpdateDestTable5Cmd(v.col1, v.col2, v.col3))
         |          })
         |      })
         |  }
         |
         |  override def delete(tableName: String, rowid: String): Unit = {
         |    getDeletes(tableName, rowid)
         |      .foreach({
         |        case x: DeleteDestTable1Cmd =>
         |          dest.deleteDestTable1().invoke(x)
         |        case x: DeleteDestTable2Cmd =>
         |          dest.deleteDestTable2().invoke(x)
         |        case x: DeleteDestTable5Cmd =>
         |          dest.deleteDestTable5().invoke(x)
         |      })
         |  }
         |}
       """.stripMargin.trim)
  }

  it should "generate insert dest-table from rowid" in {
    val table =
      <src-table name="src_table_1">
        <!--
          filter-key columns, or rowid
        -->
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <filter-key>
              <column name="col_1" type="string"/>
              <column name="col_2" type="long"/>
            </filter-key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <filter-key>
            <column name="col_1" type="string"/>
          </filter-key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <filter-key>
              <column name="col_1" type="string"/>
            </filter-key>
          </dest-table>
        </view>
      </src-table>

    insertFromRowId(table) should be(
      s"""
         |src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
         |  .map(t => {
         |    addDelete(tableName, rowid, DeleteDestTable1Cmd(t.col1, t.col2))
         |    dest.createDestTable1().invoke(CreateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |    src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |      .map(v => {
         |        addDelete(tableName, rowid, DeleteDestTable2Cmd(v.col1, v.col2))
         |        dest.createDestTable2().invoke(CreateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |      })
         |    src.querySrcView2ByCol1().invoke(querySrcView2ByCol1Cmd(t.col1))
         |      .map(v => {
         |        addDelete(tableName, rowid, DeleteDestTable5Cmd(v.col1))
         |        dest.createDestTable5().invoke(CreateDestTable5Cmd(v.col1, v.col2, v.col3))
         |      })
         |  })
       """.stripMargin.trim)
  }

  it should "generate insert dest-table from src-table" in {
    val table =
      <src-table name="src_table_1">
        <!--
          filter-key columns, or rowid
        -->
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <filter-key>
              <column name="col_1" type="string"/>
              <column name="col_2" type="long"/>
            </filter-key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <filter-key>
            <column name="col_1" type="string"/>
          </filter-key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <filter-key>
              <column name="col_1" type="string"/>
            </filter-key>
          </dest-table>
        </view>
      </src-table>

    insertFromTableMapping(table, "t") should be(
      s"""
         |addDelete(tableName, rowid, DeleteDestTable1Cmd(t.col1, t.col2))
         |dest.createDestTable1().invoke(CreateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |  .map(v => {
         |    addDelete(tableName, rowid, DeleteDestTable2Cmd(v.col1, v.col2))
         |    dest.createDestTable2().invoke(CreateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |  })
         |src.querySrcView2ByCol1().invoke(querySrcView2ByCol1Cmd(t.col1))
         |  .map(v => {
         |    addDelete(tableName, rowid, DeleteDestTable5Cmd(v.col1))
         |    dest.createDestTable5().invoke(CreateDestTable5Cmd(v.col1, v.col2, v.col3))
         |  })
       """.stripMargin.trim)
  }

  it should "generate insert dest-table from view" in {
    val table =
      <view name="src_view_1">
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <dest-table name="dest_table_2">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
      </view>

    insertFromView(table) should be(
      s"""
         |src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |  .map(v => {
         |    addDelete(tableName, rowid, DeleteDestTable1Cmd(v.col1, v.col2))
         |    dest.createDestTable1().invoke(CreateDestTable1Cmd(v.col1, v.col2, v.col3, v.col4))
         |    addDelete(tableName, rowid, DeleteDestTable2Cmd(v.col1, v.col2))
         |    dest.createDestTable2().invoke(CreateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |  })
       """.stripMargin.trim)
  }

  it should "generate insert dest-table" in {
    val table =
      <dest-table name="dest_table_1">
        <column no="1" name="col_1" from-column="col_1"/>
        <column no="2" name="col_2" from-column="col_2"/>
        <column no="3" name="col_3" from-column="col_3"/>
        <column no="4" name="col_4" from-column="col_4"/>
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
      </dest-table>

    insertDestinationTable(table, "x") should be(
      s"""
         |addDelete(tableName, rowid, DeleteDestTable1Cmd(x.col1, x.col2))
         |dest.createDestTable1().invoke(CreateDestTable1Cmd(x.col1, x.col2, x.col3, x.col4))
       """.stripMargin.trim)
  }

  it should "generate update dest-table from rowid" in {
    val table =
      <src-table name="src_table_1">
        <!--
          filter-key columns, or rowid
        -->
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <filter-key>
              <column name="col_1" type="string"/>
              <column name="col_2" type="long"/>
            </filter-key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <filter-key>
            <column name="col_1" type="string"/>
          </filter-key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <filter-key>
              <column name="col_1" type="string"/>
            </filter-key>
          </dest-table>
        </view>
      </src-table>

    updateFromRowId(table) should be(
      s"""
         |src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(rowid))
         |  .map(t => {
         |    dest.updateDestTable1().invoke(UpdateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |    src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |      .map(v => {
         |        dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |      })
         |    src.querySrcView2ByCol1().invoke(querySrcView2ByCol1Cmd(t.col1))
         |      .map(v => {
         |        dest.updateDestTable5().invoke(UpdateDestTable5Cmd(v.col1, v.col2, v.col3))
         |      })
         |  })
       """.stripMargin.trim)
  }

  it should "generate update dest-table from src-table" in {
    val table =
      <src-table name="src_table_1">
        <!--
          filter-key columns, or rowid
        -->
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <filter-key>
              <column name="col_1" type="string"/>
              <column name="col_2" type="long"/>
            </filter-key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <filter-key>
            <column name="col_1" type="string"/>
          </filter-key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <filter-key>
              <column name="col_1" type="string"/>
            </filter-key>
          </dest-table>
        </view>
      </src-table>

    updateFromTableMapping(table, "t") should be(
      s"""
         |dest.updateDestTable1().invoke(UpdateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |  .map(v => {
         |    dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |  })
         |src.querySrcView2ByCol1().invoke(querySrcView2ByCol1Cmd(t.col1))
         |  .map(v => {
         |    dest.updateDestTable5().invoke(UpdateDestTable5Cmd(v.col1, v.col2, v.col3))
         |  })
       """.stripMargin.trim)
  }

  it should "generate update dest-table from view" in {
    val table =
      <view name="src_view_1">
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <dest-table name="dest_table_2">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
      </view>

    updateFromView(table) should be(
      s"""
         |src.querySrcView1ByCol1Col2().invoke(querySrcView1ByCol1Col2Cmd(t.col1, t.col2))
         |  .map(v => {
         |    dest.updateDestTable1().invoke(UpdateDestTable1Cmd(v.col1, v.col2, v.col3, v.col4))
         |    dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |  })
       """.stripMargin.trim)
  }

  it should "generate update dest-table" in {
    val table =
      <dest-table name="dest_table_1">
        <column no="1" name="col_1" from-column="col_1"/>
        <column no="2" name="col_2" from-column="col_2"/>
        <column no="3" name="col_3" from-column="col_3"/>
        <column no="4" name="col_4" from-column="col_4"/>
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
      </dest-table>

    updateDestinationTable(table, "x") should be(
      s"""
         |dest.updateDestTable1().invoke(UpdateDestTable1Cmd(x.col1, x.col2, x.col3, x.col4))
       """.stripMargin.trim)
  }

  it should "generate delete from dest-table from rowid" in {
    val table =
      <src-table name="src_table_1">
        <!--
          filter-key columns, or rowid
        -->
        <filter-key>
          <column name="col_1" type="string"/>
          <column name="col_2" type="long"/>
        </filter-key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <filter-key>
            <column name="col_1" type="string"/>
            <column name="col_2" type="long"/>
          </filter-key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <filter-key>
              <column name="col_1" type="string"/>
              <column name="col_2" type="long"/>
            </filter-key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <filter-key>
            <column name="col_1" type="string"/>
          </filter-key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <filter-key>
              <column name="col_1" type="string"/>
            </filter-key>
          </dest-table>
        </view>
      </src-table>

    deleteFromRowId(table) should be(
      s"""
         |getDeletes(tableName, rowid)
         |  .foreach({
         |    case x: DeleteDestTable1Cmd =>
         |      dest.deleteDestTable1().invoke(x)
         |    case x: DeleteDestTable2Cmd =>
         |      dest.deleteDestTable2().invoke(x)
         |    case x: DeleteDestTable5Cmd =>
         |      dest.deleteDestTable5().invoke(x)
         |  })
       """.stripMargin.trim)
  }

  it should "generate parameter substitutions" in {
    val params = Seq(
      "table_id",
      "column_one",
      "column_two",
      "column_three"
    )
    val alias = "x"

    paramSubstitutions(params, alias) should be("x.tableId, x.columnOne, x.columnTwo, x.columnThree")
  }

  it should "generate by column names postfix" in {
    val params = Seq(
      "table_id",
      "column_one",
      "column_two",
      "column_three"
    )

    by(params) should be("TableIdColumnOneColumnTwoColumnThree")
  }

  it should "extract filter-key columns from dest-table" in {
    val table =
      <dest-table>
        <column no="1" name="column_1" from-column="column_1"/>
        <column no="2" name="column_2" from-column="column_2"/>
        <column no="3" name="column_3" from-column="column_3"/>
        <column no="4" name="column_4" from-column="column_4"/>
        <filter-key>
          <column name="column_1" type="string"/>
          <column name="column_2" type="long"/>
        </filter-key>
      </dest-table>

    filterKeyColumns(table) should be(Seq(("column_1", "string"), ("column_2", "long")))
    filterKeyColumnNames(table) should be(Seq("column_1", "column_2"))
  }

  it should "extract empty filter-key columns from src-table with no filter-key" in {
    val table =
      <src-table>
      </src-table>

    filterKeyColumns(table) should be(Seq())
    filterKeyColumnNames(table) should be(Seq())
  }

  it should "extract empty filter-key columns from src-table with empty filter-key" in {
    val table =
      <src-table>
        <filter-key>
        </filter-key>
      </src-table>

    filterKeyColumns(table) should be(Seq())
    filterKeyColumnNames(table) should be(Seq())
  }

  it should "extract filter-key columns from src-table" in {
    val table =
      <src-table>
        <filter-key>
          <column name="column_1" type="string"/>
          <column name="column_2" type="long"/>
        </filter-key>
      </src-table>

    filterKeyColumns(table) should be(Seq(("column_1", "string"), ("column_2", "long")))
    filterKeyColumnNames(table) should be(Seq("column_1", "column_2"))
  }

  it should "extract filter-key columns from view" in {
    val table =
      <view>
        <filter-key>
          <column name="column_1" type="string"/>
          <column name="column_2" type="long"/>
        </filter-key>
      </view>

    filterKeyColumns(table) should be(Seq(("column_1", "string"), ("column_2", "long")))
    filterKeyColumnNames(table) should be(Seq("column_1", "column_2"))
  }
}
