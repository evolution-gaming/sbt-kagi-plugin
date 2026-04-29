name := "sbt-kagi-plugin"
organization := "com.evolution"

description := "Simple SBT dependency lock plugin"
startYear := Some(2026)
homepage := Some(url("https://github.com/evolution-gaming/sbt-kagi-plugin"))
licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

organizationName := "Evolution"
organizationHomepage := Some(url("https://evolution.com"))

// Maven Central requires <developers> in published pom.xml files
developers := List(
  Developer(
    id = "migesok",
    name = "Mikhail Sokolov",
    email = "mikhail.g.sokolov@gmail.com",
    url = url("https://github.com/migesok"),
  ),
)

scmInfo := Some(ScmInfo(
  browseUrl = url("https://github.com/evolution-gaming/sbt-kagi-plugin"),
  connection = "git@github.com:evolution-gaming/sbt-kagi-plugin.git",
))

// DO NOT CHANGE THIS SETTING UNLESS YOU FULLY UNDERSTAND THE CONSEQUENCES!
//
// WARNING: BinaryCompatible is used instead of BinaryAndSourceCompatible because BinaryAndSourceCompatible fails
// on new methods added to objects, which doesn't really break neither source, nor binary compatibility.
// So the source compatibility should be guaranteed manually.
versionPolicyIntention := Compatibility.BinaryCompatible

// setting up sbt 1 and 2 cross-build
// https://www.scala-sbt.org/2.x/docs/en/changes/migrating-from-sbt-1.x.html#cross-building-sbt-plugin-with-sbt-1x
crossScalaVersions += "3.8.2" // version used by SBT 2.0.0-RC12
pluginCrossBuild / sbtVersion := {
  scalaBinaryVersion.value match {
    // set minimal supported sbt 1 version
    // pre 1.4.5 sbt doesn't work on M1 macs: https://github.com/sbt/sbt/issues/6162
    case "2.12" => "1.4.5"
    case "3" => "2.0.0-RC12"
  }
}

// scalac and scaladoc options
autoAPIMappings := true
scalacOptions ++= Seq(
  "-release:17", // sbt 2 requires JDK 17+
  "-deprecation",
)
scalacOptions ++= crossSettings(
  scalaVersion = scalaVersion.value,
  if2 = Seq(
    "-Xsource:3",
  ),
  // Good compiler options for Scala 2.12 are coming from com.evolution:sbt-scalac-opts-plugin:0.0.9,
  // but its support for Scala 3 is limited, especially what concerns linting options.
  //
  // If Scala 3 is made the primary target, good linting scalac options for it should be added first.
  if3 = Seq(
    "-Xkind-projector:underscores",

    // disable new brace-less syntax:
    // https://alexn.org/blog/2022/10/24/scala-3-optional-braces/
    "-no-indent",

    // improve error messages:
    "-explain",
    "-explain-types",
  ),
)

libraryDependencies += "org.scalameta" %% "munit" % "1.3.0" % Test
// disable parallel test execution and bufferization to have nice linear stdout:
// https://scalameta.org/munit/docs/tests.html#run-tests-in-parallel
Test / parallelExecution := false
Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "-b")

enablePlugins(SbtPlugin)
// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  "-Dplugin.version=" + version.value,
  "-Dsbt.color=never", // to get reliable test results on sbt log
)

addCommandAlias("fmt", "+all scalafmtAll scalafmtSbt")
addCommandAlias("buildFast", "+all scalafmtCheckAll scalafmtSbtCheck versionPolicyCheck Compile/doc test")
addCommandAlias("buildFull", "; buildFast; +scripted")

def crossSettings[T](scalaVersion: String, if3: T, if2: T): T = {
  scalaVersion match {
    case version if version.startsWith("3") => if3
    case _ => if2
  }
}
