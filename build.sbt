import Dependencies._

name         := "context-mapping-gen"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

lazy val root = (project in file("."))
  .aggregate(
    play,
    lagom,
    util,
  )

lazy val play = (project in file("play"))
  .dependsOn(util)
  .enablePlugins(ProtobufPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
lazy val lagom = (project in file("lagom"))
  .dependsOn(util)
  .enablePlugins(ProtobufPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
lazy val util = (project in file("util"))
  .enablePlugins(ProtobufPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
publishTo := sonatypePublishTo.value

