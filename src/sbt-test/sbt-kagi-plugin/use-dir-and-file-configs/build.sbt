ThisBuild / scalaVersion := "2.13.18"
ThisBuild / kagiDependencyLockDir := (ThisBuild / baseDirectory).value / "custom-locks"
ThisBuild / libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.38.9",
)

lazy val root = project
  .in(file("."))
  .aggregate(
    moduleWithDefaultFile,
    moduleWithCustomFile,
  )
  .settings(
    name := "use-dir-and-file-configs",
  )

lazy val moduleWithDefaultFile = project
  .in(file("module-with-default-file"))
  .enablePlugins(KagiPlugin)

lazy val moduleWithCustomFile = project
  .in(file("module-with-custom-file"))
  .enablePlugins(KagiPlugin)
  .settings(
    kagiDependencyLockFile := (ThisBuild / baseDirectory).value / "module-with-custom-file-1.lock.txt",
  )
