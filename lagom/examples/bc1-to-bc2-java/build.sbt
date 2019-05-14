import Dependencies._

name         := "bc1-to-bc2"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    `bc1-to-bc2-api`,
    `bc1-to-bc2-impl`,
    `bc1-to-bc2-app`
  )

lazy val `bc1-to-bc2-api` = (project in file("bc1-to-bc2-api"))
  .enablePlugins(LagomJava)
lazy val `bc1-to-bc2-impl` = (project in file("bc1-to-bc2-impl"))
  .dependsOn(`bc1-to-bc2-api`)
  .enablePlugins(ProtobufPlugin)
lazy val `bc1-to-bc2-app` = (project in file("bc1-to-bc2-app"))
  .dependsOn(`bc1-to-bc2-impl`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
