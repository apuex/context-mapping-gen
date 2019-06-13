import Dependencies._

name         := "src-dest-mapping"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val root = (project in file("."))
  .aggregate(
    `api`,
    `impl`
  )

lazy val `api` = (project in file("api"))
  .enablePlugins(LagomScala)
lazy val `impl` = (project in file("impl"))
  .dependsOn(`api`)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
