ThisBuild / scalaVersion := "2.13.18"

lazy val root = project
  .in(file("."))
  .settings(
    name := "smoke-test",
  )
  .aggregate(
    libModule,
    svc1Module,
    svc2Module,
  )

lazy val libModule = project
  .in(file("lib"))
  .settings(
    libraryDependencies += "commons-io" % "commons-io" % "2.22.0",
  )

lazy val svc1Module = project
  .in(file("svc1"))
  .enablePlugins(KagiPlugin)
  .dependsOn(
    libModule,
  )
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.20.0",
  )

lazy val svc2Module = project
  .in(file("svc2"))
  .enablePlugins(KagiPlugin)
  .settings(
    libraryDependencies += "co.fs2" %% "fs2-core" % "3.12.2",
  )
