package com.evolution.sbt.kagi

import scala.collection.immutable.TreeSet

class KagiDependencyTest extends munit.FunSuite {

  test("valid construction") {
    val dep = KagiDependency("org.example", "lib", "1.0")
    assertEquals(dep.groupId, "org.example")
    assertEquals(dep.artifactId, "lib")
    assertEquals(dep.version, "1.0")
  }

  test("toString format") {
    val dep = KagiDependency("org.example", "lib", "1.0")
    assertEquals(dep.toString, "org.example:lib:1.0")
  }

  test("blank groupId is rejected") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: groupId must contain only alphanum and punctuation chars without ':', got '  '",
    ) {
      KagiDependency("  ", "lib", "1.0")
    }
  }

  test("empty groupId is rejected") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: groupId must contain only alphanum and punctuation chars without ':', got ''",
    ) {
      KagiDependency("", "lib", "1.0")
    }
  }

  test("colon in groupId is rejected") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: groupId must contain only alphanum and punctuation chars without ':', got 'org:example'",
    ) {
      KagiDependency("org:example", "lib", "1.0")
    }
  }

  test("blank artifactId is rejected") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: artifactId must contain only alphanum and punctuation chars without ':', got ''",
    ) {
      KagiDependency("org.example", "", "1.0")
    }
  }

  test("colon in artifactId is rejected") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: artifactId must contain only alphanum and punctuation chars without ':', got 'lib:extra'",
    ) {
      KagiDependency("org.example", "lib:extra", "1.0")
    }
  }

  test("blank version is rejected") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: version must contain only alphanum and punctuation chars without ':', got ''",
    ) {
      KagiDependency("org.example", "lib", "")
    }
  }

  test("colon in version is rejected") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: version must contain only alphanum and punctuation chars without ':', got '1.0:extra'",
    ) {
      KagiDependency("org.example", "lib", "1.0:extra")
    }
  }

  test("ordering sorts by groupId, then artifactId, then version") {
    val deps = TreeSet(
      KagiDependency("z.group", "lib", "1.0"),
      KagiDependency("a.group", "z-lib", "1.0"),
      KagiDependency("a.group", "a-lib", "2.0"),
      KagiDependency("a.group", "a-lib", "1.0"),
    )
    assertEquals(
      deps.toList,
      List(
        KagiDependency("a.group", "a-lib", "1.0"),
        KagiDependency("a.group", "a-lib", "2.0"),
        KagiDependency("a.group", "z-lib", "1.0"),
        KagiDependency("z.group", "lib", "1.0"),
      ),
    )
  }

  test("parse valid string") {
    val dep = KagiDependency.parse("org.example:lib:1.0")
    assertEquals(dep, KagiDependency("org.example", "lib", "1.0"))
  }

  test("parse fails on too few tokens") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: expected 3 elements separated by ':'",
    ) {
      KagiDependency.parse("org.example:lib")
    }
  }

  test("parse with extra colons fails validation") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: version must contain only alphanum and punctuation chars without ':', got '1.0:extra'",
    ) {
      KagiDependency.parse("org.example:lib:1.0:extra")
    }
  }

  test("parse empty string fails") {
    intercept[RuntimeException] {
      KagiDependency.parse("")
    }
  }
}
