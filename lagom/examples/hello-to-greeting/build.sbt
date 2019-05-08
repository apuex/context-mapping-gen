import Dependencies._

name         := "hello-to-greeting"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := "xtwxy@hotmail.com"

lazy val root = (project in file("."))
  .aggregate(
    `hello-to-greeting-api`,
    `hello-to-greeting-impl`,
    `hello-to-greeting-app`
  )

lazy val `hello-to-greeting-api` = (project in file("hello-to-greeting-api"))
  .enablePlugins(LagomJava)
lazy val `hello-to-greeting-impl` = (project in file("hello-to-greeting-impl"))
  .dependsOn(`hello-to-greeting-api`)
  .enablePlugins(ProtobufPlugin)
lazy val `hello-to-greeting-app` = (project in file("hello-to-greeting-app"))
  .dependsOn(`hello-to-greeting-impl`)
  .enablePlugins(PlayScala)

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
publishTo := localRepo
