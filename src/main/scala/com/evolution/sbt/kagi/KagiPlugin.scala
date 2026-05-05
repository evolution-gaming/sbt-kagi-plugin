package com.evolution.sbt.kagi

import com.evolution.sbt.kagi.KagiDependencySet.DiffElem
import com.evolution.sbt.kagi.KagiSbtCompat.*
import sbt.*
import sbt.Keys.*

import java.nio.file.Files

object KagiPlugin extends AutoPlugin {

  object autoImport {
    val kagiDependencyLockDir: SettingKey[File] = settingKey(
      "FIXME",
    )
    // TODO: WIP make concrete project lock file name configurable (multiple Scala versions?)
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

    kagiDependencyLockDir := {
      val dir = (ThisBuild / baseDirectory).value / "dependency-lock"
      Files.createDirectories(dir.toPath)
      dir
    },
    kagiDependencyLockClasspath := Runtime,
  )

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    kagiDependencyLockWrite := kagiDependencyLockWriteTask.value,
    kagiDependencyLockCheck := kagiDependencyLockCheckTask.value,
  )

  private final case class ProjectCtx(
    dependencySet: KagiDependencySet,
    dependencyLockFile: File,
  )

  private lazy val kagiDependencyLockCtxTask: Def.Initialize[Task[ProjectCtx]] = Def.taskDyn {
    val config = kagiDependencyLockClasspath.value
    val projectName = thisProject.value.id
    val lockDir = kagiDependencyLockDir.value

    Def.task {
      val dependencySet = KagiDependencySet.from(
        (config / externalDependencyClasspath).value.view.map(extractDependency),
      )

      val lockFile = lockDir / s"$projectName.lock.txt"

      ProjectCtx(dependencySet, lockFile)
    }
  }

  private lazy val kagiDependencyLockWriteTask: Def.Initialize[Task[Unit]] = Def.task {
    val ctx = kagiDependencyLockCtxTask.value
    KagiLockFile.write(ctx.dependencyLockFile.toPath, ctx.dependencySet)
  }

  private lazy val kagiDependencyLockCheckTask: Def.Initialize[Task[Unit]] = Def.task {
    val ctx = kagiDependencyLockCtxTask.value
    val logger = streams.value.log
    val lockFileDependencySet = KagiLockFile.read(ctx.dependencyLockFile.toPath)

    if (ctx.dependencySet != lockFileDependencySet) {
      val diff = ctx.dependencySet.diffFrom(lockFileDependencySet)
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
        s"dependency lock file not in sync - ${ diff.size } changes: ${ ctx.dependencyLockFile }"

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
