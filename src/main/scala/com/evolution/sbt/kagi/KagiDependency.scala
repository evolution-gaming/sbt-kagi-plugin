package com.evolution.sbt.kagi

import sbt.*

private[kagi] final case class KagiDependency(
  groupId: String,
  artifactId: String,
  version: String,
) {
  import KagiDependency.*

  require(
    DependencyCoordinateRegexPredicate.test(groupId),
    s"groupId must $DependencyCoordinateRegexDescr, got '$groupId'",
  )
  require(
    DependencyCoordinateRegexPredicate.test(artifactId),
    s"artifactId must $DependencyCoordinateRegexDescr, got '$artifactId'",
  )
  require(
    DependencyCoordinateRegexPredicate.test(version),
    s"version must $DependencyCoordinateRegexDescr, got '$version'",
  )

  override def toString: String = s"$groupId:$artifactId:$version"
}

private[kagi] object KagiDependency {
  private val DependencyCoordinateRegexPredicate = """^[\p{Graph}&&[^:]]+$""".r.pattern.asMatchPredicate()
  private val DependencyCoordinateRegexDescr =
    "contain only alphanum and punctuation chars without ':'"

  implicit val DependencyOrdering: Ordering[KagiDependency] =
    Ordering.by(v => (v.groupId, v.artifactId, v.version))

  def fromModuleId(id: ModuleID): KagiDependency = {
    KagiDependency(
      groupId = id.organization,
      artifactId = id.name,
      version = id.revision,
    )
  }

  def parse(str: String): KagiDependency = {
    val tokens = str.split(":", 3)
    require(tokens.length == 3, "expected 3 elements separated by ':'")

    KagiDependency(
      groupId = tokens(0),
      artifactId = tokens(1),
      version = tokens(2),
    )
  }
}
