package com.evolution.sbt.kagi

import sbt.*

/**
 * Cross-sbt-version compat code - sbt 1 version.
 */
private[kagi] object KagiSbtCompat {
  def getModelIdAttr(attributed: Attributed[?]): Option[ModuleID] = {
    attributed.get(Keys.moduleID.key)
  }
}
