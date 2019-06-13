import Dependencies._

name         := "src-dest-mapping-api"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    sbRuntime,
    ctxmap,
    playEvents,
    scalapbRuntime % "protobuf",
    scalapbJson4s,
    scalaTest      % Test
  )
}

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
