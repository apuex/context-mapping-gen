import sbt._

object Dependencies {
  lazy val scalaVersionNumber    = "2.12.8"
  lazy val akkaVersion           = "2.5.22"
  lazy val artifactGroupName     = "com.apuex.sales.mapping"
  lazy val artifactVersionNumber = "1.0.0"
  lazy val sprayVersion          = "1.3.5"
  lazy val playVersion           = "2.7.2"
  lazy val lagomVersion          = "1.5.0"

  lazy val scalaXml        = "org.scala-lang.modules"    %%  "scala-xml"                           % "1.0.6"
  lazy val akkaActor       = "com.typesafe.akka"         %%  "akka-actor"                          % akkaVersion
  lazy val akkaRemote      = "com.typesafe.akka"         %%  "akka-remote"                         % akkaVersion
  lazy val akkaStream      = "com.typesafe.akka"         %%  "akka-stream"                         % akkaVersion
  lazy val akkaPersistence = "com.typesafe.akka"         %%  "akka-persistence"                    % akkaVersion
  lazy val akkaPersistenceQuery = "com.typesafe.akka"    %% "akka-persistence-query"               % akkaVersion
  lazy val akkaPersistenceCassandra = "com.typesafe.akka"         %%  "akka-persistence-cassandra"          % "0.93"
  lazy val akkaPersistenceCassandraLauncher = "com.typesafe.akka"         %%  "akka-persistence-cassandra-launcher" % "0.93"
  lazy val akkaCluster     = "com.typesafe.akka"         %%  "akka-cluster"                        % akkaVersion
  lazy val akkaClusterTools= "com.typesafe.akka"         %%  "akka-cluster-tools"                  % akkaVersion
  lazy val akkaClusterMetrics = "com.typesafe.akka"         %%  "akka-cluster-metrics"                        % akkaVersion
  lazy val akkaClusterSharding = "com.typesafe.akka"         %%  "akka-cluster-sharding"               % akkaVersion
  lazy val akkaSlf4j       = "com.typesafe.akka"         %%  "akka-slf4j"                          % akkaVersion
  lazy val akkaTestkit     = "com.typesafe.akka"         %%  "akka-testkit"                        % akkaVersion
  lazy val play            = "com.typesafe.play"         %%  "play"                                % playVersion
  lazy val playTest        = "com.typesafe.play"         %%  "play-test"                           % playVersion
  lazy val jodaTime        = "joda-time"                 %   "joda-time"                           % "2.10.1"
  lazy val googleGuice     = "com.google.inject"         %   "guice"                               % "4.2.0"
  lazy val playGuice       = "com.typesafe.play"         %%  "play-guice"                          % playVersion
  lazy val playJson        = "com.typesafe.play"         %%  "play-json"                           % playVersion
  lazy val lagomApi        = "com.lightbend.lagom"       %%  "lagom-scaladsl-api"                  % lagomVersion

  lazy val playSocketIO    = "com.lightbend.play"        %%  "play-socket-io"                      % "1.0.0-beta-2"
  lazy val macwireMicros   = "com.softwaremill.macwire"  %%  "macros"                              % "2.3.0"
  lazy val guava           = "com.google.guava"          %   "guava"                               % "22.0"
  lazy val slf4jApi        = "org.slf4j"                 %  "slf4j-api"                            % "1.7.25"
  lazy val slf4jSimple     = "org.slf4j"                 %  "slf4j-simple"                         % "1.7.25"
  lazy val logbackClassic  = "ch.qos.logback"            %   "logback-classic"                     % "1.2.3"
  lazy val scalaTest       = "org.scalatest"             %% "scalatest"                            % "3.0.4"
  lazy val scalaTesplusPlay= "org.scalatestplus.play"    %%  "scalatestplus-play"                  % "3.1.2"
  lazy val scalacheck      = "org.scalacheck"            %%  "scalacheck"                          % "1.13.4"
  lazy val scalaTestPlusPlay = "org.scalatestplus.play"  %%  "scalatestplus-play"                  % "3.1.2"

  lazy val dependedRepos = Seq(
      "Atlassian Releases" at "https://maven.atlassian.com/public/",
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("snapshots")
  )

  lazy val confPath = "../conf"
  lazy val localRepo = Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
}
