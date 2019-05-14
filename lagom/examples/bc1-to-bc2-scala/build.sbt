import Dependencies._

name         := "bc1-to-bc2-scala"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    `app`,
    `impl`,
    `app`
  )

lazy val `api` = (project in file("api"))
  .enablePlugins(ProtobufPlugin)
  .enablePlugins(LagomScala)
lazy val `impl` = (project in file("impl"))
  .dependsOn(`api`)
  .enablePlugins(ProtobufPlugin)
lazy val `app` = (project in file("app"))
  .dependsOn(`impl`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
