package com.evolution.sbt.kagi

import sbt.*

private[kagi] object KagiSbtCompat {
  def getModelIdAttr(attributed: Attributed[?]): Option[ModuleID] = {
    attributed.get(Keys.moduleID.key)
  }
}
