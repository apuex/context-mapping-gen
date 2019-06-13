package com.github.apuex.ctxmapgen.table

import com.github.apuex.ctxmapgen.util.ClasspathXmlLoader
import org.scalatest.{FlatSpec, Matchers}

class MappingLoaderSpec extends FlatSpec with Matchers {
  "A MappingLoader" should "load mapping project settings from xml" in {
    val m = MappingLoader(ClasspathXmlLoader("com/github/apuex/ctxmapgen/table/mappings.xml").xml)
    import m._

    mapping should be("mapping")
    srcSystem should be("src")
    destSystem should be("dest")
    modelName should be("src_dest_mapping")
    modelPackage should be("com.github.apuex.mapping")
    modelVersion should be("1.0.0")
    modelMaintainer should be("xtwxy@hotmail.com")
    outputDir should be(s"${System.getProperty("output.dir", "target/generated")}")
    rootProjectDir should be(s"${outputDir}/src-dest-mapping")
    apiProjectName should be("src-dest-mapping-api")
    apiProjectDir should be("target/generated/src-dest-mapping/api")
    apiSrcPackage should be("com.github.apuex.mapping")
    apiSrcDir should be(s"${outputDir}/src-dest-mapping/api/src/main/scala/com/github/apuex/mapping")
    implProjectName should be("src-dest-mapping-impl")
    implProjectDir should be("target/generated/src-dest-mapping/impl")
    implSrcPackage should be("com.github.apuex.mapping.impl")
    implSrcDir should be(s"${outputDir}/src-dest-mapping/impl/src/main/scala/com/github/apuex/mapping/impl")
    hyphen should be(if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-")
  }
}
