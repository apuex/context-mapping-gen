import Dependencies._

name         := "context-mapping-gen"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    play,
    lagom,
    table,
    util,
    runtime
  )

lazy val play = (project in file("play"))
  .dependsOn(util)
  .enablePlugins(GraalVMNativeImagePlugin)
lazy val lagom = (project in file("lagom"))
  .dependsOn(util)
  .enablePlugins(GraalVMNativeImagePlugin)
lazy val table = (project in file("table"))
  .dependsOn(util)
  .enablePlugins(GraalVMNativeImagePlugin)
lazy val util = (project in file("util"))
lazy val runtime = (project in file("runtime"))

publishTo := sonatypePublishTo.value

