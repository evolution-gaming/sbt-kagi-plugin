package com.evolution.sbt.kagi

import scala.collection.compat.IterableOnce
import scala.collection.immutable.{TreeMap, TreeSet}
import scala.collection.mutable

/**
 * [[KagiPlugin]] data model - a sorted set of [[KagiDependency]].
 *
 * Use `diff*` methods to find the difference between dependency sets!
 *
 * @param elems
 *   dependency set elements as a [[TreeSet]]
 *
 * @see
 *   [[KagiDependencySet.DiffElem]]
 */
private[kagi] final case class KagiDependencySet(elems: TreeSet[KagiDependency]) {
  import KagiDependencySet.*

  def size: Int = elems.size

  /**
   * Find the difference from the given dependency set to this one, implying this set is
   * the newer version.
   *
   * @see
   *   [[KagiDependencySet.diff]]
   * @see
   *   [[KagiDependencySet.DiffElem]]
   */
  def diffFrom(other: KagiDependencySet): TreeMap[KagiDependency, DiffElem] = {
    diff(from = other, to = this)
  }
}

private[kagi] object KagiDependencySet {
  val empty: KagiDependencySet = KagiDependencySet(TreeSet.empty[KagiDependency])

  def from(col: IterableOnce[KagiDependency]): KagiDependencySet = {
    // the impl has to work on both Scala 2.12 (sbt 1) and 3 (sbt 2)
    val builder = TreeSet.newBuilder[KagiDependency]
    builder ++= col
    KagiDependencySet(builder.result())
  }

  def apply(dependencies: KagiDependency*): KagiDependencySet = {
    from(dependencies)
  }

  /**
   * Find the difference from one dependency set to another.
   *
   * The result denotes the changes needed to be made to produce the target set from the
   * initial one.
   *
   * @see
   *   [[KagiDependencySet.DiffElem]]
   */
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

  /**
   * A dependency change from one dependency set to another.
   *
   * @see
   *   [[KagiDependencySet.diff]]
   */
  sealed trait DiffElem
  object DiffElem {

    /**
     * A dependency added.
     */
    case object Added extends DiffElem

    /**
     * A dependency removed.
     */
    case object Removed extends DiffElem

    /**
     * Version changed for a dependency (groupId + artifactId stays the same).
     *
     * @param newVersion
     *   new version string
     */
    final case class VersionChanged(newVersion: String) extends DiffElem
  }
}
