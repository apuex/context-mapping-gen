
import Dependencies._

name         := "hello-to-greeting-impl"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= {
  Seq(
    playEvents,
    playGuice,
    serializer,
    akkaPersistence,
    akkaPersistenceQuery,
    akkaClusterSharding,
    macwire        % Provided,
    scalaTest      % Test
  )
}
