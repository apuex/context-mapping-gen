import Dependencies._

name := "hello"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

lazy val `hello` = (project in file("."))
  .enablePlugins(LagomScala)

libraryDependencies ++= Seq(
  scalaXml,
  sbRuntime,
  slf4jSimple % Test,
  scalaTest % Test
)

publishTo := sonatypePublishTo.value

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.rename
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some(s"${artifactGroupName}.hello.Main")
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

