import Dependencies._

name := "context-mapping-gen-play"
scalaVersion := scalaVersionNumber
organization := artifactGroupName
version      := artifactVersionNumber
maintainer   := artifactMaintainer

libraryDependencies ++= Seq(
  scalaXml,
  sbRuntime,
  slf4jSimple % Test,
  scalaTest % Test
)

publishTo := sonatypePublishTo.value

graalVMNativeImageOptions ++= Seq(
  "-H:+ReportUnsupportedElementsAtRuntime",
  "-H:IncludeResources=.*conf",
  "-H:IncludeResources=.*\\.properties",
  "-H:IncludeResources=.*\\.xml",
  "-H:IncludeResourceBundles=com.sun.org.apache.xerces.internal.impl.msg.XMLMessages",
  "-H:ReflectionConfigurationFiles=" + baseDirectory.value / "graal" / "reflection-xml.json"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.rename
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.github.apuex.ctxmapgen.play.Main")
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

