package com.evolution.sbt.kagi

import scala.collection.compat.IterableOnce
import scala.collection.immutable.{TreeMap, TreeSet}
import scala.collection.mutable

private[kagi] final case class KagiDependencySet(elems: TreeSet[KagiDependency]) {
  import KagiDependencySet.*

  def size: Int = elems.size

  def diffFrom(other: KagiDependencySet): TreeMap[KagiDependency, DiffElem] = {
    diff(from = other, to = this)
  }
}

private[kagi] object KagiDependencySet {
  val empty: KagiDependencySet = KagiDependencySet(TreeSet.empty[KagiDependency])

  def from(col: IterableOnce[KagiDependency]): KagiDependencySet = {
    val builder = TreeSet.newBuilder[KagiDependency]
    builder ++= col
    KagiDependencySet(builder.result())
  }

  def apply(dependencies: KagiDependency*): KagiDependencySet = {
    from(dependencies)
  }

  def diff(from: KagiDependencySet, to: KagiDependencySet): TreeMap[KagiDependency, DiffElem] = {
    // the impl coalesces removed-added pairs for the same (groupId, artifactId) into VersionChanged
    // going from the removed set and pairing removed versions with added ones in the sorting order

    val removedGrouped = (from.elems -- to.elems).groupBy(dep => (dep.groupId, dep.artifactId))
    val added = to.elems -- from.elems
    val addedGrouped = added.groupBy(dep => (dep.groupId, dep.artifactId))
    val remainingAdded = mutable.HashSet(added.toVector*)

    val builder = TreeMap.newBuilder[KagiDependency, DiffElem]
    def add(dep: KagiDependency, diffElem: DiffElem): Unit = {
      builder += (dep -> diffElem)
    }

    removedGrouped.foreach {
      case (key, removedKeyDeps) =>
        addedGrouped.get(key) match {
          case None =>
            removedKeyDeps.foreach(add(_, DiffElem.Removed))
          case Some(addedKeyDeps) =>
            val removedKeyDepsVec = removedKeyDeps.toVector
            removedKeyDepsVec.zip(addedKeyDeps).foreach { case (removedDep, addedDep) =>
              add(removedDep, DiffElem.VersionChanged(newVersion = addedDep.version))
              remainingAdded -= addedDep
            }
            removedKeyDepsVec.drop(addedKeyDeps.size).foreach(add(_, DiffElem.Removed))
        }
    }
    remainingAdded.foreach(add(_, DiffElem.Added))

    builder.result()
  }

  sealed trait DiffElem
  object DiffElem {
    case object Added extends DiffElem
    case object Removed extends DiffElem
    final case class VersionChanged(newVersion: String) extends DiffElem
  }
}
