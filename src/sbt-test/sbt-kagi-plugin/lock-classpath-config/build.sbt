lazy val root = (project in file(".")).enablePlugins(KagiPlugin).settings(
  name := "lock-classpath-config",
  // This dependency is only in Runtime, not in Compile
  libraryDependencies += "com.google.guava" % "guava" % "33.4.0-jre" % Runtime,
  // Lock against Compile classpath, so guava should NOT appear in lock file
  kagiDependencyLockClasspath := Compile,
)
