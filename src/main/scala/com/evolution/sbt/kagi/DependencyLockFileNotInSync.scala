package com.evolution.sbt.kagi

import sbt.*

import scala.util.control.NoStackTrace

/**
 * Fails the [[KagiPlugin.autoImport.kagiDependencyLockCheck]] task if the actual module
 * dependency set differs from the one written in the lock file.
 */
final case class DependencyLockFileNotInSync private[kagi] (message: String)
extends RuntimeException(message)
with FeedbackProvidedException // so the exception is printed only once by sbt
with NoStackTrace
