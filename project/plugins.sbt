// The Play plugin
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.10")

// checks style
addSbtPlugin("org.xerial.sbt" % "sbt-jcheckstyle" % "0.2.1")
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "10.7.0"

// formats code
addSbtPlugin("com.github.sbt" % "sbt-java-formatter" % "0.10.0")

