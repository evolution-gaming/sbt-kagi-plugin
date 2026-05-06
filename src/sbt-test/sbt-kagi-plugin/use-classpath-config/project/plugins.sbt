sys.props.get("plugin.version") match {
  case Some(v) =>
    addSbtPlugin("com.evolution" % "sbt-kagi-plugin" % v)
  case None =>
    sys.error("""|The system property 'plugin.version' is not defined.
                 |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
