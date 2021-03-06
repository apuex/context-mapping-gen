
import Dependencies._
import sbtassembly.MergeStrategy

name         := "bc1-to-bc2-api"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    protobufUtil,
    scalaTest      % Test
  )
}

