
import Dependencies._
import sbtassembly.MergeStrategy

name         := "hello-to-greeting-api"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := "xtwxy@hotmail.com"

libraryDependencies ++= {
  Seq(
    protobufUtil,
    scalaTest      % Test
  )
}

