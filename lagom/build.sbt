import Dependencies._

name := "context-mapping-gen-lagom"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber

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

mainClass in assembly := Some("com.github.apuex.ctxmapgen.lagom.Main")
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"