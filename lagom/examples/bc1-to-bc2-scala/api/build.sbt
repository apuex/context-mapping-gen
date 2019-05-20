
import Dependencies._

name         := "bc1-to-bc2-mapping-api"
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
       
