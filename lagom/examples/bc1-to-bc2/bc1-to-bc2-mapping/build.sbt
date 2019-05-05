
import Dependencies._
import sbtassembly.MergeStrategy

name         := "bc1-to-bc2-mapping"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := "xtwxy@hotmail.com"

libraryDependencies ++= {
  Seq(
    akkaPersistence,
    akkaPersistenceQuery,
    akkaPersistenceCassandra,
    akkaPersistenceCassandraLauncher,
    akkaClusterSharding,
    lagomApi,
    playGuice,
    scalaTest      % Test
  )
}


assemblyJarName in assembly := s"${name.value}-assembly-${version.value}.jar"
mainClass in assembly := Some("play.core.server.ProdServerStart")

assemblyMergeStrategy in assembly := {
  case manifest if manifest.contains("MANIFEST.MF") =>
    // We don't need manifest files since sbt-assembly will create
    // one with the given settings
    MergeStrategy.discard
  case PathList("META-INF", "io.netty.versions.properties") =>
    MergeStrategy.discard
  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
    // Keep the content for all reference-overrides.conf files
    MergeStrategy.concat
  case x =>
    // For all the other files, use the default sbt-assembly merge strategy
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
