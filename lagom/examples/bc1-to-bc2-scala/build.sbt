import Dependencies._

name         := "bc1-to-bc2-mapping"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    `api`,
    `impl`,
    `app`
  )

lazy val `api` = (project in file("api"))
  .enablePlugins(LagomScala)
lazy val `impl` = (project in file("impl"))
  .dependsOn(`api`)
lazy val `app` = (project in file("app"))
  .dependsOn(`impl`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
