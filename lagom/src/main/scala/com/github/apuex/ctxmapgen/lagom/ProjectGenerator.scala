package com.github.apuex.ctxmapgen.lagom

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters.cToShell

class ProjectGenerator(mappingLoader: MappingLoader) {

  import mappingLoader._

  def generate(): Unit = {
    rootProjectSettings()
    apiProjectSettings()
    appProjectSettings()
    implProjectSettings()
  }

  def apiProjectSettings(): Unit = {
    if (new File(s"${apiProjectDir}/build.sbt").exists()) return
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
         |    protobufUtil,
         |    scalaTest      % Test
         |  )
         |}
       """.stripMargin
    )
    printWriter.close()
  }

  def appProjectSettings(): Unit = {
    if (new File(s"${appProjectDir}/build.sbt").exists()) return
    new File(appProjectDir).mkdirs()
    val printWriter = new PrintWriter(s"${appProjectDir}/build.sbt", "utf-8")
    printWriter.println(
      s"""
         |import Dependencies._
         |import sbtassembly.MergeStrategy
         |
         |name         := "${appProjectName}"
         |scalaVersion := scalaVersionNumber
         |organization := artifactGroupName
         |version      := artifactVersionNumber
         |maintainer   := "xtwxy@hotmail.com"
         |
         |libraryDependencies ++= {
         |  Seq(
         |    guice,
         |    scalaTest      % Test
         |  )
         |}
         |
         |assemblyJarName in assembly := s"$${name.value}-assembly-$${version.value}.jar"
         |mainClass in assembly := Some("play.core.server.ProdServerStart")
         |fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
         |
         |assemblyExcludedJars in assembly := {
         |  val cp = (fullClasspath in assembly).value
         |  cp.filter( x =>
         |    x.data.getName.contains("javax.activation-api")
         |      || x.data.getName.contains("play-logback")
         |  )
         |}
         |
         |assemblyMergeStrategy in assembly := {
         |  case manifest if manifest.contains("MANIFEST.MF") =>
         |    // We don't need manifest files since sbt-assembly will create
         |    // one with the given settings
         |    MergeStrategy.discard
         |  case PathList("META-INF", "io.netty.versions.properties") =>
         |    MergeStrategy.discard
         |  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
         |    // Keep the content for all reference-overrides.conf files
         |    MergeStrategy.concat
         |  case x =>
         |    // For all the other files, use the default sbt-assembly merge strategy
         |    val oldStrategy = (assemblyMergeStrategy in assembly).value
         |    oldStrategy(x)
         |}
       """.stripMargin
    )
    printWriter.close()
  }

  def implProjectSettings(): Unit = {
    if (new File(s"${implProjectDir}/build.sbt").exists()) return
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
         |maintainer   := "xtwxy@hotmail.com"
         |
         |libraryDependencies ++= {
         |  Seq(
         |    akkaPersistence,
         |    akkaPersistenceQuery,
         |    akkaPersistenceCassandra,
         |    akkaPersistenceCassandraLauncher,
         |    akkaClusterSharding,
         |    scalaTest      % Test
         |  )
         |}
         |
         |assemblyJarName in assembly := s"$${name.value}-assembly-$${version.value}.jar"
         |mainClass in assembly := Some("play.core.server.ProdServerStart")
         |
         |assemblyExcludedJars in assembly := {
         |  val cp = (fullClasspath in assembly).value
         |  cp.filter( x =>
         |    x.data.getName.contains("javax.activation-api")
         |      || x.data.getName.contains("play-logback")
         |  )
         |}
         |
         |assemblyMergeStrategy in assembly := {
         |  case manifest if manifest.contains("MANIFEST.MF") =>
         |    // We don't need manifest files since sbt-assembly will create
         |    // one with the given settings
         |    MergeStrategy.discard
         |  case PathList("META-INF", "io.netty.versions.properties") =>
         |    MergeStrategy.discard
         |  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
         |    // Keep the content for all reference-overrides.conf files
         |    MergeStrategy.concat
         |  case x =>
         |    // For all the other files, use the default sbt-assembly merge strategy
         |    val oldStrategy = (assemblyMergeStrategy in assembly).value
         |    oldStrategy(x)
         |}
       """.stripMargin
    )
    printWriter.close()
  }


  def rootProjectSettings(): Unit = {
    if (!new File(s"${rootProjectDir}/build.sbt").exists())
    // build.sbt
    rootProjectBuildSbt()
    if (!new File(s"${rootProjectDir}/project/build.properties").exists())
    rootProjectBuildProperties()
    if (!new File(s"${rootProjectDir}/project/plugin.sbt").exists())
    rootProjectPluginSbt()
    if (!new File(s"${rootProjectDir}/project/Dependencies.scala").exists())
    rootProjectDependencies
  }

  def makeRootProjectDir(): Boolean = new File(s"${rootProjectDir}/project/").mkdirs()

  def rootProjectDependencies(): Unit = {
    makeRootProjectDir()
    val printWriter = new PrintWriter(s"${rootProjectDir}/project/Dependencies.scala", "utf-8")
    printWriter.println(
      s"""
         |import sbt._
         |
         |object Dependencies {
         |  lazy val scalaVersionNumber    = "2.12.8"
         |  lazy val akkaVersion           = "2.5.22"
         |  lazy val artifactGroupName     = "com.apuex.sales.mapping"
         |  lazy val artifactVersionNumber = "1.0.0"
         |  lazy val sprayVersion          = "1.3.5"
         |  lazy val playVersion           = "2.7.2"
         |  lazy val lagomVersion          = "1.5.0"
         |
         |  lazy val scalaXml        = "org.scala-lang.modules"    %%  "scala-xml"                           % "1.0.6"
         |  lazy val akkaActor       = "com.typesafe.akka"         %%  "akka-actor"                          % akkaVersion
         |  lazy val akkaRemote      = "com.typesafe.akka"         %%  "akka-remote"                         % akkaVersion
         |  lazy val akkaStream      = "com.typesafe.akka"         %%  "akka-stream"                         % akkaVersion
         |  lazy val akkaPersistence = "com.typesafe.akka"         %%  "akka-persistence"                    % akkaVersion
         |  lazy val akkaPersistenceQuery = "com.typesafe.akka"    %% "akka-persistence-query"               % akkaVersion
         |  lazy val akkaPersistenceCassandra = "com.typesafe.akka"         %%  "akka-persistence-cassandra"          % "0.93"
         |  lazy val akkaPersistenceCassandraLauncher = "com.typesafe.akka"         %%  "akka-persistence-cassandra-launcher" % "0.93"
         |  lazy val akkaCluster     = "com.typesafe.akka"         %%  "akka-cluster"                        % akkaVersion
         |  lazy val akkaClusterTools= "com.typesafe.akka"         %%  "akka-cluster-tools"                  % akkaVersion
         |  lazy val akkaClusterMetrics = "com.typesafe.akka"         %%  "akka-cluster-metrics"                        % akkaVersion
         |  lazy val akkaClusterSharding = "com.typesafe.akka"         %%  "akka-cluster-sharding"               % akkaVersion
         |  lazy val akkaSlf4j       = "com.typesafe.akka"         %%  "akka-slf4j"                          % akkaVersion
         |  lazy val akkaTestkit     = "com.typesafe.akka"         %%  "akka-testkit"                        % akkaVersion
         |  lazy val play            = "com.typesafe.play"         %%  "play"                                % playVersion
         |  lazy val playTest        = "com.typesafe.play"         %%  "play-test"                           % playVersion
         |  lazy val jodaTime        = "joda-time"                 %   "joda-time"                           % "2.10.1"
         |  lazy val googleGuice     = "com.google.inject"         %   "guice"                               % "4.2.0"
         |  lazy val playGuice       = "com.typesafe.play"         %%  "play-guice"                          % playVersion
         |  lazy val playJson        = "com.typesafe.play"         %%  "play-json"                           % playVersion
         |  lazy val lagomApi        = "com.lightbend.lagom"       %%  "lagom-scaladsl-api"                  % lagomVersion
         |
         |  lazy val playSocketIO    = "com.lightbend.play"        %%  "play-socket-io"                      % "1.0.0-beta-2"
         |  lazy val macwireMicros   = "com.softwaremill.macwire"  %%  "macros"                              % "2.3.0"
         |  lazy val guava           = "com.google.guava"          %   "guava"                               % "22.0"
         |  lazy val slf4jApi        = "org.slf4j"                 %  "slf4j-api"                            % "1.7.25"
         |  lazy val slf4jSimple     = "org.slf4j"                 %  "slf4j-simple"                         % "1.7.25"
         |  lazy val logbackClassic  = "ch.qos.logback"            %   "logback-classic"                     % "1.2.3"
         |  lazy val scalaTest       = "org.scalatest"             %% "scalatest"                            % "3.0.4"
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
         |addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "0.6.3")
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
         |maintainer   := "xtwxy@hotmail.com"
         |
         |lazy val root = (project in file("."))
         |  .aggregate(
         |    `${implProjectName}`,
         |    `${appProjectName}`
         |  )
         |
         |lazy val `${implProjectName}` = (project in file("${implProjectName}"))
         |  .enablePlugins(ProtobufPlugin)
         |  .enablePlugins(LagomScala)
         |lazy val `${appProjectName}` = (project in file("${appProjectName}"))
         |  .dependsOn(`${implProjectName}`)
         |  .enablePlugins(PlayScala)
         |
         |resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
         |publishTo := localRepo
       """.stripMargin
        .trim
    )
    printWriter.close()
  }
}
