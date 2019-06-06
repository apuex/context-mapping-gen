package com.github.apuex.ctxmapgen.table

import com.github.apuex.ctxmapgen.util.ClasspathXmlLoader
import org.scalatest.{FlatSpec, Matchers}

class TableMappingGeneratorSpec extends FlatSpec with Matchers {
  val m = TableMappingGenerator(MappingLoader(ClasspathXmlLoader("com/github/apuex/ctxmapgen/table/mappings.xml").xml))

  import m._

  "A TableMappingGenerator" should "generate table mapping from rowid" in {
    fail("test not implemented.")
  }

  it should "generate insert dest-table from rowid" in {
    fail("test not implemented.")
  }

  it should "generate insert dest-table from src-table" in {
    fail("test not implemented.")
  }

  it should "generate insert dest-table from view" in {
    val table =
      <view name="src_view_1">
        <key>
          <column name="col_1"/>
          <column name="col_2"/>
        </key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
        </dest-table>
        <dest-table name="dest_table_2">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
        </dest-table>
      </view>

    insertFromView(table) should be(
      s"""
         |src.retrieveSrcView1().invoke(RetrieveSrcView1Cmd(t.col1, t.col2))
         |  .map(v => {
         |    dest.createDestTable1().invoke(CreateDestTable1Cmd(v.col1, v.col2, v.col3, v.col4))
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
        <key>
          <column name="col_1"/>
          <column name="col_2"/>
        </key>
      </dest-table>

    insertDestinationTable(table, "x") should be(
      s"""
         |dest.createDestTable1().invoke(CreateDestTable1Cmd(x.col1, x.col2, x.col3, x.col4))
       """.stripMargin.trim)
  }

  it should "generate update dest-table from rowid" in {
    val table =
      <src-table name="src_table_1">
        <!--
          key columns, or rowid
        -->
        <key>
          <column name="col_1"/>
          <column name="col_2"/>
        </key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <key>
              <column name="col_1"/>
              <column name="col_2"/>
            </key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <key>
            <column name="col_1"/>
          </key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <key>
              <column name="col_1"/>
            </key>
          </dest-table>
        </view>
      </src-table>

    updateFromRowId(table) should be(
      s"""
         |src.retrieveSrcTable1ByRowid().invoke(RetrieveByRowidCmd(evt.rowid))
         |  .map(t => {
         |    dest.updateDestTable1().invoke(UpdateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |    src.retrieveSrcView1().invoke(RetrieveSrcView1Cmd(t.col1, t.col2))
         |      .map(v => {
         |        dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |      })
         |    src.retrieveSrcView2().invoke(RetrieveSrcView2Cmd(t.col1))
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
          key columns, or rowid
        -->
        <key>
          <column name="col_1"/>
          <column name="col_2"/>
        </key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
        </dest-table>
        <!--
          affected views by source table change.
        -->
        <view name="src_view_1">
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
          <dest-table name="dest_table_2">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <column no="4" name="col_4" from-column="col_4"/>
            <key>
              <column name="col_1"/>
              <column name="col_2"/>
            </key>
          </dest-table>
        </view>
        <view name="src_view_2">
          <key>
            <column name="col_1"/>
          </key>
          <dest-table name="dest_table_5">
            <column no="1" name="col_1" from-column="col_1"/>
            <column no="2" name="col_2" from-column="col_2"/>
            <column no="3" name="col_3" from-column="col_3"/>
            <key>
              <column name="col_1"/>
            </key>
          </dest-table>
        </view>
      </src-table>

    updateFromTableMapping(table, "t") should be(
      s"""
         |dest.updateDestTable1().invoke(UpdateDestTable1Cmd(t.col1, t.col2, t.col3, t.col4))
         |src.retrieveSrcView1().invoke(RetrieveSrcView1Cmd(t.col1, t.col2))
         |  .map(v => {
         |    dest.updateDestTable2().invoke(UpdateDestTable2Cmd(v.col1, v.col2, v.col3, v.col4))
         |  })
         |src.retrieveSrcView2().invoke(RetrieveSrcView2Cmd(t.col1))
         |  .map(v => {
         |    dest.updateDestTable5().invoke(UpdateDestTable5Cmd(v.col1, v.col2, v.col3))
         |  })
       """.stripMargin.trim)
  }

  it should "generate update dest-table from view" in {
    val table =
      <view name="src_view_1">
        <key>
          <column name="col_1"/>
          <column name="col_2"/>
        </key>
        <dest-table name="dest_table_1">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
        </dest-table>
        <dest-table name="dest_table_2">
          <column no="1" name="col_1" from-column="col_1"/>
          <column no="2" name="col_2" from-column="col_2"/>
          <column no="3" name="col_3" from-column="col_3"/>
          <column no="4" name="col_4" from-column="col_4"/>
          <key>
            <column name="col_1"/>
            <column name="col_2"/>
          </key>
        </dest-table>
      </view>

    updateFromView(table) should be(
      s"""
         |src.retrieveSrcView1().invoke(RetrieveSrcView1Cmd(t.col1, t.col2))
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
        <key>
          <column name="col_1"/>
          <column name="col_2"/>
        </key>
      </dest-table>

    updateDestinationTable(table, "x") should be(
      s"""
         |dest.updateDestTable1().invoke(UpdateDestTable1Cmd(x.col1, x.col2, x.col3, x.col4))
       """.stripMargin.trim)
  }

  it should "generate delete from dest-table from rowid" in {
    val table =
      <dest-table name="dest_table_1">
        <column no="1" name="col_1" from-column="col_1"/>
        <column no="2" name="col_2" from-column="col_2"/>
        <column no="3" name="col_3" from-column="col_3"/>
        <column no="4" name="col_4" from-column="col_4"/>
        <key>
          <column name="col_1"/>
          <column name="col_2"/>
        </key>
      </dest-table>

    fail("test not implemented.")
    deleteFromRowId(table) should be("-")
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

  it should "extract key columns from dest-table" in {
    val table =
      <dest-table>
        <column no="1" name="column_1" from-column="column_1"/>
        <column no="2" name="column_2" from-column="column_2"/>
        <column no="3" name="column_3" from-column="column_3"/>
        <column no="4" name="column_4" from-column="column_4"/>
        <key>
          <column name="column_1"/>
          <column name="column_2"/>
        </key>
      </dest-table>

    keyColumns(table) should be(Seq("column_1", "column_2"))
  }

  it should "extract empty key columns from src-table with no key" in {
    val table =
      <src-table>
      </src-table>

    keyColumns(table) should be(Seq())
  }

  it should "extract empty key columns from src-table with empty key" in {
    val table =
      <src-table>
        <key>
        </key>
      </src-table>

    keyColumns(table) should be(Seq())
  }

  it should "extract key columns from src-table" in {
    val table =
      <src-table>
        <key>
          <column name="column_1"/>
          <column name="column_2"/>
        </key>
      </src-table>

    keyColumns(table) should be(Seq("column_1", "column_2"))
  }

  it should "extract key columns from view" in {
    val table =
      <view>
        <key>
          <column name="column_1"/>
          <column name="column_2"/>
        </key>
      </view>

    keyColumns(table) should be(Seq("column_1", "column_2"))
  }
}
