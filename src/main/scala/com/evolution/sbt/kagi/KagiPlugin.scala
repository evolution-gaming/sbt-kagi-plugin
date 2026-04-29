package com.evolution.sbt.kagi

import com.evolution.sbt.kagi.Model.*
import sbt.*
import sbt.Keys.*

import java.nio.file.Files
import scala.collection.immutable.TreeSet

object KagiPlugin extends AutoPlugin {

  object autoImport {
    val kagiDependencyLockDir: SettingKey[File] = settingKey(
      "FIXME",
    )
//    val kagiDependencyLockConfiguration: SettingKey[Configuration] = settingKey(
//      "FIXME"
//    )
    val kagiDependencyLockWrite: TaskKey[Unit] = taskKey(
      "FIXME",
    )
    val kagiDependencyLockCheck: TaskKey[Unit] = taskKey(
      "FIXME",
    )
  }

  import autoImport.*

  override lazy val globalSettings: Seq[Def.Setting[?]] = Seq(
    // Provide default values in globalSettings:
    // https://www.scala-sbt.org/1.x/docs/Plugins-Best-Practices.html#Provide+default+values+in

    kagiDependencyLockDir :=
      {
        val dir = (ThisBuild / baseDirectory).value / "dependency-lock"
        Files.createDirectories(dir.toPath)
        dir
      },
//    kagiDependencyLockConfiguration := Runtime,
  )

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    kagiDependencyLockWrite := kagiDependencyLockWriteTask.value,
    kagiDependencyLockCheck := kagiDependencyLockCheckTask.value,
  )

  private lazy val kagiDependencyLockWriteTask = Def.task {
    val projectName = thisProject.value.id // TODO: WIP copy-paste
    //    val config = kagiDependencyLockConfiguration.value
    val configName = Runtime.name

    val dependencySet = (Runtime / externalDependencyClasspath).value.map(extractDependency).to[TreeSet]

    val lockFile = kagiDependencyLockDir.value / s"$projectName-$configName.lock.txt"

    KagiLockFile.write(lockFile.toPath, dependencySet)
  }

  private lazy val kagiDependencyLockCheckTask = Def.task {
    val projectName = thisProject.value.id // TODO: WIP copy-paste
    //    val config = kagiDependencyLockConfiguration.value
    val configName = Runtime.name

    val dependencySet = (Runtime / externalDependencyClasspath).value.map(extractDependency).to[TreeSet]

    val lockFile = kagiDependencyLockDir.value / s"$projectName-$configName.lock.txt"

    val lockFileDependencySet = KagiLockFile.read(lockFile.toPath)

    if (dependencySet != lockFileDependencySet) {
      // TODO: WIP use custom exception without stacktrace, print nice diff
      sys.error("dependency lock file not in sync")
    }
  }

  private def extractDependency(attributed: Attributed[?]): KagiDependency = {
    val depIds = attributed.metadata.entries.collect {
      case AttributeEntry(_, value: ModuleID) => KagiDependency.fromModuleId(value)
    }.toSet
    require(depIds.size == 1, s"$depIds")
    depIds.head
  }
}
