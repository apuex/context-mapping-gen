
import Dependencies._

name         := "hello-to-greeting-api"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    playEvents,
    scalapbRuntime % "protobuf",
    scalapbJson4s,
    scalaTest      % Test
  )
}

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
