import Dependencies._
import sbtassembly.MergeStrategy

name         := "bc1-to-bc2-mapping-impl"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    playEvents,
    playGuice,
    akkaPersistence,
    akkaPersistenceQuery,
    akkaClusterSharding,
    macwire        % Provided,
    scalaTest      % Test
  )
}
