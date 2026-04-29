lazy val root = (project in file(".")).enablePlugins(KagiPlugin).settings(
  name := "check-without-lock",
  libraryDependencies += "com.google.guava" % "guava" % "33.4.0-jre" % Runtime,
)
