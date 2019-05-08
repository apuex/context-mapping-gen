
import Dependencies._
import sbtassembly.MergeStrategy

name         := "hello-to-greeting-impl"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := "xtwxy@hotmail.com"

libraryDependencies ++= {
  Seq(
    playEvents,
    playGuice,
    serializer,
    akkaPersistence,
    akkaPersistenceQuery,
    akkaClusterSharding,
    macwire        % Provided,
    scalaTest      % Test
  )
}

