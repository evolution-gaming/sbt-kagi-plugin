package com.evolution.sbt.kagi

import sbt.*

import scala.collection.immutable.TreeSet

private[kagi] object Model {
  type KagiDependencySet = TreeSet[KagiDependency]

  final case class KagiDependency(
    groupId: String,
    artifactId: String,
    version: String,
  ) {
    // TODO: WIP validate arguments

    override def toString: String = s"$groupId:$artifactId:$version"
  }

  object KagiDependency {
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

      if (tokens.length != 3) {
        sys.error("expected 3 elements separated by :")
      }

      KagiDependency(
        groupId = tokens(0),
        artifactId = tokens(1),
        version = tokens(2),
      )
    }
  }
}
