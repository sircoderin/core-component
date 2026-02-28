name := "core-component"
organization := "dot.cpp"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  guice,
  "dot.cpp" %% "repository-component" % "1.0",
  "org.playframework" %% "play-mailer" % "10.1.0",
  "org.playframework" %% "play-mailer-guice" % "10.1.0",
  // play framework required jjwt 0.11.5
  "io.jsonwebtoken" % "jjwt-api" % "0.11.5",
  "io.jsonwebtoken" % "jjwt-impl" % "0.11.5",
  "io.jsonwebtoken" % "jjwt-gson" % "0.11.5",
  "com.password4j" % "password4j" % "1.8.4",
)


Global / onChangedBuildSource := ReloadOnSourceChanges
jcheckStyleConfig := "google-checks.xml"

// compile will run formatter and checkstyle on app files and test files
(Compile / compile) := ((Compile / compile) dependsOn (Compile / javafmt)).value
(Compile / compile) := ((Compile / compile) dependsOn (Compile / jcheckStyle)).value
(Compile / compile) := ((Compile / compile) dependsOn (Test / jcheckStyle)).value
