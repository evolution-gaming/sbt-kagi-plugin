lazy val root = (project in file(".")).enablePlugins(KagiPlugin).settings(
  name := "lock-file-config",
  libraryDependencies += "com.google.guava" % "guava" % "33.4.0-jre" % Runtime,
  kagiDependencyLockDir := baseDirectory.value / "custom-locks",
  kagiDependencyLockFile := kagiDependencyLockDir.value / "my-custom.lock.txt",
)
