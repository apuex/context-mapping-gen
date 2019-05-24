addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.5.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.20")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.20")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0-M5"
