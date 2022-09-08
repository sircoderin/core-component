// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.16")

// checks style
addSbtPlugin("org.xerial.sbt" % "sbt-jcheckstyle" % "0.2.1")
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "8.31"

// formats code
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.6.0")
