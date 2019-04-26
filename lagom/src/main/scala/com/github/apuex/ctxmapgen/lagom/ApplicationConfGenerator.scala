package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

class ApplicationConfGenerator(mappingLoader: MappingLoader) {
  import mappingLoader._
  def generate(): Unit = {
    new File(applicationConfDir).mkdirs()
    val printWriter = new PrintWriter(s"${applicationConfDir}/application.conf", "utf-8")
    printWriter.println(
      s"""
         |# https://www.playframework.com/documentation/latest/Configuration
         |play {
         |  http {
         |    secret {
         |      key="cfd16c3a-f0f2-4fa9-8e58-ff9a2ad2a422"
         |      key=$${? APPLICATION_SECRET}
         |    }
         |  }
         |  filters {
         |    hosts {
         |      allowed=["localhost", "192.168.0.78"]
         |    }
         |    headers {
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
         |    actor-system = "${system}"
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
         |      ${system}-protobuf = "akka.remote.serialization.ProtobufSerializer"
         |    }
         |
         |    serialization-bindings {
         |      "java.io.Serializable" = none
         |      "scalapb.GeneratedMessage" = ${system}-protobuf
         |    }
         |  }
         |
         |  alarm {
         |    eval-period = 5 seconds
         |  }
         |
         |  remote {
         |    log-remote-lifecycle-events = off
         |    artery.untrusted-mode = off
         |    log-sent-messages = off
         |
         |    netty.tcp {
         |      hostname = "192.168.0.78"
         |      port = 2553
         |      hostname = $${? HOSTNAME}
         |      port = $${? PORT}
         |    }
         |  }
         |
         |  akka {
         |    actor {
         |      number-of-shards = 100
         |      provider = "akka.cluster.ClusterActorRefProvider"
         |    }
         |
         |    cluster {
         |      seed-nodes = [
         |        "akka.tcp://${system}@192.168.0.78:2553"
         |      ]
         |    }
         |  }
         |
         |}
       """.stripMargin)
    printWriter.close()
  }
}
