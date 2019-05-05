
import Dependencies._
import sbtassembly.MergeStrategy

name         := "bc1-to-bc2-impl"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := "xtwxy@hotmail.com"

libraryDependencies ++= {
  Seq(
    akkaPersistence,
    akkaPersistenceQuery,
    akkaClusterSharding,
    macwire        % Provided,
    scalaTest      % Test
  )
}

