import Dependencies._

name := "context-mapping-gen-util"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

libraryDependencies ++= Seq(
  scalaXml,
  sbRuntime,
  slf4jSimple % Test,
  scalaTest % Test
)

publishTo := sonatypePublishTo.value
