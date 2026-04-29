package com.evolution.sbt.kagi

import scala.collection.immutable.TreeMap

class KagiDependencySetTest extends munit.FunSuite {

  import KagiDependencySet.DiffElem

  private val depA1 = KagiDependency("com.example", "lib-a", "1.0")
  private val depA2 = KagiDependency("com.example", "lib-a", "2.0")
  private val depA3 = KagiDependency("com.example", "lib-a", "3.0")
  private val depB = KagiDependency("com.example", "lib-b", "1.0")
  private val depC = KagiDependency("org.other", "lib-c", "3.0")

  private val emptyDiff = mkDiff()

  // -- diff test cases - start

  test("diff: empty to empty") {
    val result = KagiDependencySet.diff(KagiDependencySet.empty, KagiDependencySet.empty)
    assertEquals(result, emptyDiff)
  }

  test("diff: empty to non-empty marks all as Added") {
    val to = KagiDependencySet(depA1, depB)
    val result = KagiDependencySet.diff(KagiDependencySet.empty, to)
    assertEquals(
      result,
      mkDiff(
        depA1 -> DiffElem.Added,
        depB -> DiffElem.Added,
      ),
    )
  }

  test("diff: non-empty to empty marks all as Removed") {
    val from = KagiDependencySet(depA1, depB)
    val result = KagiDependencySet.diff(from, KagiDependencySet.empty)
    assertEquals(
      result,
      mkDiff(
        depA1 -> DiffElem.Removed,
        depB -> DiffElem.Removed,
      ),
    )
  }

  test("diff: identical sets produce empty diff") {
    val set = KagiDependencySet(depA1, depB)
    val result = KagiDependencySet.diff(set, set)
    assertEquals(result, emptyDiff)
  }

  test("diff: version change is detected") {
    val from = KagiDependencySet(depA1)
    val to = KagiDependencySet(depA2)
    val result = KagiDependencySet.diff(from, to)
    assertEquals(
      result,
      mkDiff(
        depA1 -> DiffElem.VersionChanged(newVersion = "2.0"),
      ),
    )
  }

  test("diff: mixed added, removed, changed, and unchanged") {
    val from = KagiDependencySet(depA1, depB)
    val to = KagiDependencySet(depA2, depC)
    val result = KagiDependencySet.diff(from, to)
    assertEquals(
      result,
      mkDiff(
        depA1 -> DiffElem.VersionChanged(newVersion = "2.0"),
        depB -> DiffElem.Removed,
        depC -> DiffElem.Added,
      ),
    )
  }

  test("diff: mixed added, removed, changed, and unchanged - multiple versions in from set") {
    val from = KagiDependencySet(depA1, depA2, depB)
    val to = KagiDependencySet(depA3, depC)
    val result = KagiDependencySet.diff(from, to)
    assertEquals(
      result,
      mkDiff(
        depA1 -> DiffElem.VersionChanged(newVersion = "3.0"),
        depA2 -> DiffElem.Removed,
        depB -> DiffElem.Removed,
        depC -> DiffElem.Added,
      ),
    )
  }

  test("diff: mixed added, removed, changed, and unchanged - multiple versions in to set") {
    val from = KagiDependencySet(depA1, depB)
    val to = KagiDependencySet(depA2, depA3, depC)
    val result = KagiDependencySet.diff(from, to)
    assertEquals(
      result,
      mkDiff(
        depA1 -> DiffElem.VersionChanged(newVersion = "2.0"),
        depA3 -> DiffElem.Added,
        depB -> DiffElem.Removed,
        depC -> DiffElem.Added,
      ),
    )
  }

  // -- diff test cases - end

  private def mkDiff(elems: (KagiDependency, DiffElem)*): TreeMap[KagiDependency, DiffElem] = {
    TreeMap(elems*)
  }
}
