
import Dependencies._

name         := "hello-to-greeting-api"
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

