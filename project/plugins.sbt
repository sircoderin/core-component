// The Play plugin
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.8")

// checks style
addSbtPlugin("org.xerial.sbt" % "sbt-jcheckstyle" % "0.2.1")
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "10.7.0"

// formats code
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.6.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")
