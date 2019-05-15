package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

import com.github.apuex.ctxmapgen.lagom.ServiceGenerator._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent

import scala.collection.mutable

class ApplicationConfGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    generateAppConf()
    generateLogConf()
    generateMessageConf()
    generateRoutesConf()
  }

  def generateAppConf(): Unit = {
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
      .trim
  }

  def generateLogConf(): Unit = {
    new File(applicationConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${applicationConfDir}/logback.xml", "utf-8")
    printWriter.println(
      s"""
         |<!-- https://www.playframework.com/documentation/latest/SettingsLogger -->
         |<configuration>
         |
         |  <conversionRule conversionWord="coloredLevel" converterClass="play.api.libs.logback.ColoredLevel" />
         |
         |  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
         |    <file>$${application.home :-.}/logs/application.log</file>
         |    <encoder>
         |      <pattern>%date [%level] from %logger in %thread - %message%n%xException</pattern>
         |    </encoder>
         |  </appender>
         |
         |  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
         |    <encoder>
         |      <pattern>%coloredLevel %logger{15} - %message%n%xException{10}</pattern>
         |    </encoder>
         |  </appender>
         |
         |  <appender name="ASYNCFILE" class="ch.qos.logback.classic.AsyncAppender">
         |    <appender-ref ref="FILE" />
         |  </appender>
         |
         |  <appender name="ASYNCSTDOUT" class="ch.qos.logback.classic.AsyncAppender">
         |    <appender-ref ref="STDOUT" />
         |  </appender>
         |
         |  <logger name="play" level="INFO" />
         |  <logger name="akka" level="INFO" />
         |  <logger name="application" level="INFO" />
         |  <logger name="${modelPackage}" level="INFO" />
         |
         |  <root level="INFO">
         |    <appender-ref ref="ASYNCSTDOUT" />
         |    <appender-ref ref="ASYNCFILE" />
         |  </root>
         |
         |</configuration>
       """.stripMargin
        .trim)
    printWriter.close()
  }

  def generateMessageConf(): Unit = {
    new File(applicationConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${applicationConfDir}/messages", "utf-8")
    printWriter.println(
      s"""
         |# https://www.playframework.com/documentation/latest/ScalaI18N
       """.stripMargin
        .trim)
    printWriter.close()
  }

  def generateRoutesConf(): Unit = {
    new File(applicationConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${applicationConfDir}/routes", "utf-8")
    printWriter.println(
      s"""
         |# Routes
         |# This file defines all application routes (Higher priority routes first)
         |# https://www.playframework.com/documentation/latest/ScalaRouting
         |# ~~~~
         |
         |# Map static resources from the /public folder to the /assets URL path
         |GET     /assets/*file               controllers.Assets.versioned(path="/public", file: Asset)
         |->      /api                        playevents.Routes
       """.stripMargin
        .trim)
    printWriter.close()
  }
}
