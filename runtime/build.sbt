import Dependencies._

name := "context-mapping-runtime"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  slf4jSimple % Test,
  scalaTest % Test
)

publishTo := sonatypePublishTo.value
