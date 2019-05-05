import Dependencies._

name         := "bc1-to-bc2"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := "xtwxy@hotmail.com"

lazy val root = (project in file("."))
  .aggregate(
    `bc1-to-bc2-mapping`,
    `bc1-to-bc2-app`
  )

lazy val `bc1-to-bc2-mapping` = (project in file("bc1-to-bc2-mapping"))
  .enablePlugins(ProtobufPlugin)
  .enablePlugins(LagomScala)
lazy val `bc1-to-bc2-app` = (project in file("bc1-to-bc2-app"))
  .dependsOn(`bc1-to-bc2-mapping`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
