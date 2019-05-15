package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.ctxmapgen.lagom.ServiceGenerator._

import scala.collection.mutable

class ApplicationConfGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    new File(applicationConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${applicationConfDir}/application.conf", "utf-8")
    printWriter.println(
      s"""
         |# https://www.playframework.com/documentation/latest/Configuration
         |
         |lagom.services {
         |  ${indent(generateServiceRefs(), 2)}
         |}
         |
         |play {
         |  application {
         |    loader = "${implSrcPackage}.${cToPascal(s"${srcSystem}_${destSystem}_${app}_${loader}")}"
         |  }
         |  http {
         |    secret {
         |      // TODO: replace it with your own key!
         |      key="cfd16c3a-f0f2-4fa9-8e58-ff9a2ad2a422"
         |      key=$${? APPLICATION_SECRET}
         |    }
         |  }
         |  filters {
         |    hosts {
         |      // TODO: replace it hosts allowed!
         |      allowed=["localhost"]
         |    }
         |    headers {
         |      // TODO: replace it your own security options!
         |      frameOptions=null
         |      xssProtection=null
         |      contentTypeOptions=null
         |      permittedCrossDomainPolicies=null
         |      contentSecurityPolicy=null
         |    }
         |  }
         |  server {
         |    http {
         |      port = 9000
         |    }
         |  }
         |  akka {
         |    actor-system = "${destSystem}"
         |  }
         |}
         |
         |akka {
         |  loggers = ["akka.event.slf4j.Slf4jLogger"]
         |  loglevel = "INFO"
         |  log-config-on-start = off
         |  log-dead-letters = 0
         |  log-dead-letters-during-shutdown = off
         |
         |  actor {
         |    passivate-timeout = 6 seconds
         |
         |    serializers {
         |      ${cToShell(destSystem)}-protobuf = "akka.remote.serialization.ProtobufSerializer"
         |    }
         |
         |    serialization-bindings {
         |      "java.io.Serializable" = none
         |      // scalapb 0.8.4
         |      "scalapb.GeneratedMessage" = ${cToShell(destSystem)}-protobuf
         |      // google protobuf-java 3.6.1
         |      "com.google.protobuf.GeneratedMessageV3" = ${cToShell(destSystem)}-protobuf
         |    }
         |  }
         |
         |  // leveldb persistence plugin for development environment.
         |  // TODO: replace it with cassandra plugins for production unless you known what you are doing.
         |  persistence {
         |    journal {
         |      plugin = "akka.persistence.journal.leveldb"
         |      leveldb {
         |        dir = "target/journal"
         |        native = on
         |        fsync = off
         |      }
         |    }
         |    snapshot-store {
         |      plugin = "akka.persistence.snapshot-store.local"
         |      local {
         |        dir = "target/snapshots"
         |        native = on
         |        fsync = on
         |      }
         |    }
         |    query {
         |      journal {
         |        leveldb {
         |          class = "akka.persistence.query.journal.leveldb.LeveldbReadJournalProvider"
         |          write-plugin="akka.persistence.journal.leveldb"
         |          dir = "target/journal"
         |          native = on
         |          // switch off fsync would not survive process crashes.
         |          fsync = off
         |          # Verify checksum on read.
         |          checksum = on
         |          // the max-buffer-size requires fine adjustments
         |          // to balance between performance and system load.
         |          max-buffer-size = 100000
         |        }
         |      }
         |    }
         |  }
         |}
         |
       """.stripMargin
        .trim)
    printWriter.close()
  }

  def generateServiceRefs(): String = {
    val serviceCalls: mutable.Map[String, mutable.Set[OperationDescription]] = mutable.Map()
    collectServiceCalls(xml, serviceCalls)
    serviceCalls.map(x => s"""${cToShell(x._1)} = "http://localhost:9000/${cToShell(x._1)}"""")
      .reduce((x, y) => "%s\n%s".format(x, y))
  }
}
