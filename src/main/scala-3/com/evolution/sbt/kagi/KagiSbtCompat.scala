package com.evolution.sbt.kagi

import sbt.*

/**
 * Cross-sbt-version compat code - sbt 2 version.
 */
private[kagi] object KagiSbtCompat {
  def getModelIdAttr(attributed: Attributed[?]): Option[ModuleID] = {
    attributed.get(Keys.moduleIDStr).map(Classpaths.moduleIdJsonKeyFormat.read)
  }
}
