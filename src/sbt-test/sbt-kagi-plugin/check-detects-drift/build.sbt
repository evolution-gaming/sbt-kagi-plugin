lazy val root = (project in file(".")).enablePlugins(KagiPlugin).settings(
  name := "check-detects-drift",
  libraryDependencies += "com.google.guava" % "guava" % "33.4.0-jre" % Runtime,
)
