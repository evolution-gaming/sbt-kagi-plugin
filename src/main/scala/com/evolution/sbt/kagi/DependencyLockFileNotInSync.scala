package com.evolution.sbt.kagi

import sbt.*

import scala.util.control.NoStackTrace

// TODO: WIP document
final case class DependencyLockFileNotInSync private[kagi] (message: String)
extends RuntimeException(message)
with FeedbackProvidedException // so the exception is printed only once by sbt
with NoStackTrace
