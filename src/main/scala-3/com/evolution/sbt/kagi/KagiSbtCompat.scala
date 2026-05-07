package com.evolution.sbt.kagi

import sbt.*

/**
 * Cross-sbt-version compat code - sbt 2 version.
 */
private[kagi] object KagiSbtCompat {
  def getModuleIdAttr(attributed: Attributed[?]): Option[ModuleID] = {
    attributed.get(Keys.moduleIDStr).map(Classpaths.moduleIdJsonKeyFormat.read)
  }
}
