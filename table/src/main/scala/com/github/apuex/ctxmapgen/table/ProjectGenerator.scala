package com.github.apuex.ctxmapgen.table

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters.cToShell

object ProjectGenerator {
  def apply(fileName: String): ProjectGenerator = new ProjectGenerator(MappingLoader(fileName))

  def apply(mappingLoader: MappingLoader): ProjectGenerator = new ProjectGenerator(mappingLoader)
}

class ProjectGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    rootProjectSettings()
    apiProjectSettings()
    implProjectSettings()
  }

  def apiProjectSettings(): Unit = {
    new File(apiProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${apiProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${apiProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    sbRuntime,
         |    ctxmap,
         |    playEvents,
         |    scalapbRuntime % "protobuf",
         |    scalapbJson4s,
         |    scalaTest      % Test
         |  )
         |}
         |
         |PB.targets in Compile := Seq(
         |  scalapb.gen() -> (sourceManaged in Compile).value
         |)
       """.stripMargin.trim
    )
    printWriter.close()
  }

  def implProjectSettings(): Unit = {
    new File(implSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${implProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |import sbtassembly.MergeStrategy
         |
         |name         := "${implProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |libraryDependencies ++= {
         |  Seq(
         |    playEvents,
         |    playGuice,
         |    akkaPersistence,
         |    akkaPersistenceQuery,
         |    akkaClusterSharding,
         |    macwire        % Provided,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin.trim
    )
    printWriter.close()
  }


  def rootProjectSettings(): Unit = {
    // build.sbt
    rootProjectBuildSbt()
    rootProjectBuildProperties()
    rootProjectPluginSbt()
    rootProjectDependencies()
  }

  def makeRootProjectDir(): Boolean = new File(s"${rootProjectDir}/project/").mkdirs()

  def rootProjectDependencies(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/project/Dependencies.scala", "utf-8")
    printWriter.println(
      s"""
         |import sbt._
         |import scalapb.compiler.Version.scalapbVersion
         |
         |object Dependencies {
         |  lazy val scalaVersionNumber    = "2.12.8"
         |  lazy val akkaVersion           = "2.5.22"
         |  lazy val artifactGroupName     = "${modelPackage}"
         |  lazy val artifactVersionNumber = "${modelVersion}"
         |  lazy val artifactMaintainer    = "xtwxy@hotmail"
         |  lazy val sprayVersion          = "1.3.5"
         |  lazy val playVersion           = "2.7.2"
         |  lazy val lagomVersion          = "1.5.0"
         |
         |  lazy val scalaXml        = "org.scala-lang.modules"    %%  "scala-xml"                           % "1.0.6"
         |  lazy val akkaActor       = "com.typesafe.akka"         %%  "akka-actor"                          % akkaVersion
         |  lazy val akkaRemote      = "com.typesafe.akka"         %%  "akka-remote"                         % akkaVersion
         |  lazy val akkaStream      = "com.typesafe.akka"         %%  "akka-stream"                         % akkaVersion
         |  lazy val akkaPersistence = "com.typesafe.akka"         %%  "akka-persistence"                    % akkaVersion
         |  lazy val leveldbjni      = "org.fusesource.leveldbjni" %   "leveldbjni-all"                      % "1.8"
         |  lazy val akkaPersistenceQuery = "com.typesafe.akka"    %%  "akka-persistence-query"              % akkaVersion
         |  lazy val akkaPersistenceCassandra = "com.typesafe.akka"%%  "akka-persistence-cassandra"          % "0.93"
         |  lazy val akkaCluster     = "com.typesafe.akka"         %%  "akka-cluster"                        % akkaVersion
         |  lazy val akkaClusterTools= "com.typesafe.akka"         %%  "akka-cluster-tools"                  % akkaVersion
         |  lazy val akkaClusterMetrics = "com.typesafe.akka"      %%  "akka-cluster-metrics"                % akkaVersion
         |  lazy val akkaClusterSharding = "com.typesafe.akka"     %%  "akka-cluster-sharding"               % akkaVersion
         |  lazy val akkaSlf4j       = "com.typesafe.akka"         %%  "akka-slf4j"                          % akkaVersion
         |  lazy val akkaTestkit     = "com.typesafe.akka"         %%  "akka-testkit"                        % akkaVersion
         |  lazy val play            = "com.typesafe.play"         %%  "play"                                % playVersion
         |  lazy val playTest        = "com.typesafe.play"         %%  "play-test"                           % playVersion
         |  lazy val jodaTime        = "joda-time"                 %   "joda-time"                           % "2.10.1"
         |  lazy val googleGuice     = "com.google.inject"         %   "guice"                               % "4.2.0"
         |  lazy val scalapbRuntime  = "com.thesamet.scalapb"      %% "scalapb-runtime"                      % scalapbVersion
         |  lazy val scalapbJson4s   = "com.thesamet.scalapb"      %% "scalapb-json4s"                       % "0.9.0-M1"
         |  lazy val playGuice       = "com.typesafe.play"         %%  "play-guice"                          % playVersion
         |  lazy val playJson        = "com.typesafe.play"         %%  "play-json"                           % playVersion
         |  lazy val lagomApi        = "com.lightbend.lagom"       %%  "lagom-scaladsl-api"                  % lagomVersion
         |  lazy val macwire         = "com.softwaremill.macwire"  %%  "macros"                              % "2.3.0"
         |
         |  lazy val sbRuntime       = "com.github.apuex.springbootsolution" %% "scala-runtime"              % "1.0.9"
         |  lazy val ctxmap          = "com.github.apuex"          %% "context-mapping-runtime"              % "1.0.0"
         |  lazy val playEvents      = "com.github.apuex"          %%  "play-events"                         % "1.0.2"
         |  lazy val serializer      = "com.github.apuex.protobuf" %   "protobuf-serializer"                 % "1.0.1"
         |  lazy val playSocketIO    = "com.lightbend.play"        %%  "play-socket-io"                      % "1.0.0-beta-2"
         |  lazy val macwireMicros   = "com.softwaremill.macwire"  %%  "macros"                              % "2.3.0"
         |  lazy val guava           = "com.google.guava"          %   "guava"                               % "22.0"
         |  lazy val slf4jApi        = "org.slf4j"                 %   "slf4j-api"                           % "1.7.25"
         |  lazy val slf4jSimple     = "org.slf4j"                 %   "slf4j-simple"                        % "1.7.25"
         |  lazy val logbackClassic  = "ch.qos.logback"            %   "logback-classic"                     % "1.2.3"
         |  lazy val scalaTest       = "org.scalatest"             %%  "scalatest"                           % "3.0.4"
         |  lazy val scalaTesplusPlay= "org.scalatestplus.play"    %%  "scalatestplus-play"                  % "3.1.2"
         |  lazy val scalacheck      = "org.scalacheck"            %%  "scalacheck"                          % "1.13.4"
         |  lazy val scalaTestPlusPlay = "org.scalatestplus.play"  %%  "scalatestplus-play"                  % "3.1.2"
         |
         |  lazy val dependedRepos = Seq(
         |      "Atlassian Releases" at "https://maven.atlassian.com/public/",
         |      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
         |      Resolver.sonatypeRepo("snapshots")
         |  )
         |
         |  lazy val confPath = "../conf"
         |  lazy val localRepo = Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
         |}
       """.stripMargin
        .trim
    )
    printWriter.close()
  }

  def rootProjectPluginSbt(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/project/plugin.sbt", "utf-8")
    printWriter.println(
      s"""
         |addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.5.0")
         |addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.2")
         |addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
         |addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.20")
         |
         |addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.20")
         |libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0-M5"
       """.stripMargin
        .trim
    )
    printWriter.close()
  }

  def rootProjectBuildProperties(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/project/build.properties", "utf-8")
    printWriter.println("sbt.version=1.2.8")
    printWriter.close()
  }

  def rootProjectBuildSbt(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |
         |name         := "${cToShell(modelName)}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := artifactMaintainer
         |
         |lazy val root = (project in file("."))
         |  .aggregate(
         |    `${api}`,
         |    `${impl}`
         |  )
         |
         |lazy val `${api}` = (project in file("${api}"))
         |  .enablePlugins(LagomScala)
         |lazy val `${impl}` = (project in file("${impl}"))
         |  .dependsOn(`${api}`)
         |
         |resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
         |publishTo := localRepo
       """.stripMargin
        .trim
    )
    printWriter.close()
  }
}
