package com.github.apuex.ctxmapgen.lagom

import com.github.apuex.ctxmapgen.util.ClasspathXmlLoader
import org.scalatest.{FlatSpec, Matchers}

class MappingLoaderSpec extends FlatSpec with Matchers {
  "A MappingLoader" should "load mapping project settings from xml" in {
    System.setProperty("symbol.naming", "unix_c")
    val m = MappingLoader(ClasspathXmlLoader("com/github/apuex/ctxmapgen/lagom/mappings.xml").xml)
    import m._

    modelName should be("bc1_to_bc2_mapping")
    modelPackage should be("com.apuex.sales.mapping")
    modelVersion should be("1.0.0")
    outputDir should be(s"${System.getProperty("output.dir", "target/generated")}")
    rootProjectName should be("bc1-to-bc2-mapping")
    rootProjectDir should be(s"${outputDir}/bc1-to-bc2-mapping")
    implProjectName should be("bc1-to-bc2-mapping-impl")
    implProjectDir should be(s"${outputDir}/bc1-to-bc2-mapping/impl")
    implSrcDir should be(s"${implProjectDir}/src/main/scala/com/apuex/sales/mapping/bc1ToBc2Mapping/impl")
    implSrcPackage should be("com.apuex.sales.mapping.bc1ToBc2Mapping.impl")
    symboConverter should be(if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}")
      "new IdentityConverter()" else "new CamelToCConverter()")
    docsDir should be(s"${outputDir}/bc1-to-bc2-mapping/docs")
    classNamePostfix should be(s"Impl")
    hyphen should be(if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-")
  }
}
