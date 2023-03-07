// The Play plugin
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.19")

// checks style
addSbtPlugin("org.xerial.sbt" % "sbt-jcheckstyle" % "0.2.1")
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "10.7.0"

// formats code
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.6.0")
