package com.evolution.sbt.kagi

import com.evolution.sbt.kagi.KagiDependencySet.DiffElem
import com.evolution.sbt.kagi.KagiSbtCompat.*
import sbt.*
import sbt.Keys.*

import java.nio.file.Files

/**
 * Simple sbt plugin for working with dependency lock files.
 *
 * The main focus is on the ease of use, readability and diff-friendliness of the lock
 * file format.
 *
 * Enable the plugin explicitly on the modules, for which you want to track dependencies.
 *
 * Run the [[autoImport.kagiDependencyLockWrite]] task to generate the lock files.
 *
 * Run the [[autoImport.kagiDependencyLockCheck]] task to verify that the actual
 * dependency sets match the lock files.
 *
 * Commit the lock files to your VCS and run [[autoImport.kagiDependencyLockCheck]] on
 * each build, to make sure the changes in dependencies are visible in your MRs/PRs.
 *
 * Check out the project README for more details!
 *
 * @see
 *   [[https://github.com/evolution-gaming/sbt-kagi-plugin]]
 */
object KagiPlugin extends AutoPlugin {

  // TODO: WIP review the README example code

  object autoImport {
    val kagiDependencyLockDir: SettingKey[File] = settingKey(
      "FIXME",
    )
    val kagiDependencyLockFile: SettingKey[File] = settingKey(
      "FIXME",
    )
    val kagiDependencyLockClasspath: SettingKey[Configuration] = settingKey(
      "Configuration used to resolve the dependency classpath for lock file generation. Defaults to Runtime.",
    )
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

    kagiDependencyLockDir := (ThisBuild / baseDirectory).value / "dependency-lock",
    kagiDependencyLockClasspath := Runtime,
  )

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    kagiDependencyLockFile := {
      val dir = kagiDependencyLockDir.value
      val projectName = thisProject.value.id
      dir / s"$projectName.lock.txt"
    },
    kagiDependencyLockWrite := kagiDependencyLockWriteTask.value,
    kagiDependencyLockCheck := kagiDependencyLockCheckTask.value,
  )

  private lazy val kagiDependencySetTask: Def.Initialize[Task[KagiDependencySet]] = Def.taskDyn {
    val config = kagiDependencyLockClasspath.value

    Def.task {
      KagiDependencySet.from(
        (config / externalDependencyClasspath).value.view.map(extractDependency),
      )
    }
  }

  private lazy val kagiDependencyLockWriteTask: Def.Initialize[Task[Unit]] = Def.task {
    val lockFile = kagiDependencyLockFile.value
    // ensure the lock file directory exists
    Option(lockFile.toPath.getParent).foreach { parentDir =>
      Files.createDirectories(parentDir)
    }
    val dependencySet = kagiDependencySetTask.value
    KagiLockFile.write(lockFile.toPath, dependencySet)
  }

  private lazy val kagiDependencyLockCheckTask: Def.Initialize[Task[Unit]] = Def.task {
    val lockFile = kagiDependencyLockFile.value
    val dependencySet = kagiDependencySetTask.value
    val logger = streams.value.log
    val lockFileDependencySet = KagiLockFile.read(lockFile.toPath)

    if (dependencySet != lockFileDependencySet) {
      val diff = dependencySet.diffFrom(lockFileDependencySet)
      val diffText = diff.iterator.map {
        case (dependency, diffElem) => diffElem match {
            case DiffElem.Added =>
              s"\t+ $dependency"
            case DiffElem.Removed =>
              s"\t- $dependency"
            case DiffElem.VersionChanged(newVersion) =>
              s"\t* $dependency -> $newVersion"
          }
      }.mkString("\n")

      val suggestionLine =
        "Run kagiDependencyLockWrite to update the lock files and commit changes to make them visible in your VCS!"

      val headerMsgLine =
        s"dependency lock file not in sync - ${ diff.size } changes: $lockFile"

      logger.error(s"$headerMsgLine\n$diffText")

      throw DependencyLockFileNotInSync(s"$headerMsgLine\n$suggestionLine")
    }
  }

  private def extractDependency(classpathElem: Attributed[?]): KagiDependency = {
    val modelId = getModelIdAttr(classpathElem).getOrElse(sys.error(
      s"classpath element missing module ID attribute: $classpathElem",
    ))
    KagiDependency.fromModuleId(modelId)
  }
}
