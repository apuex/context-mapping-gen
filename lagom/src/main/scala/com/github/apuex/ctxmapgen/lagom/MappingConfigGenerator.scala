package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

class MappingConfigGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  val mappingConfig =
    s"""
       |package ${apiSrcPackage}
       |
       |import java.util.Date
       |import java.util.concurrent.TimeUnit
       |
       |import akka.stream.scaladsl.Source
       |import akka.util.Timeout
       |
       |import scala.concurrent.duration.Duration
       |
       |class MappingConfig {
       |  implicit val duration = Duration.apply(30, TimeUnit.SECONDS)
       |  implicit val timeout = Timeout(duration)
       |  val snapshotSequenceCount: Long = 1000
       |
       |  val keepAlive = Source.fromIterator(() => new Iterator[String] {
       |    override def hasNext: Boolean = true
       |
       |    override def next(): String = s"[$${new Date()}] - keep-alive."
       |  })
       |    .throttle(1, duration)
       |    .map(_.toString)
       |}
     """.stripMargin.trim

  def generate(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implSrcDir}/MappingConfig.scala", "utf-8")
    printWriter.println(mappingConfig)
    printWriter.close()
  }
}
