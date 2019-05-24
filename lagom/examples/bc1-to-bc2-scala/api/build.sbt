
import Dependencies._

name         := "bc1-to-bc2-mapping-api"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    scalapbRuntime % "protobuf",
    scalapbJson4s,
    scalaTest      % Test
  )
}

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)