// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs

import com.caoccao.javet.interfaces.IJavetLogger
import com.intellij.openapi.diagnostic.Logger

private val LOG = Logger.getInstance("#cline.runtime")

class ClineRuntimeLogger : IJavetLogger {

  override fun debug(message: String?) {
    LOG.info(message)
  }

  override fun error(message: String?) {
    LOG.error(message)
  }

  override fun error(message: String?, cause: Throwable?) {
    LOG.error(message, cause)
  }

  override fun info(message: String?) {
    LOG.info(message)
  }

  override fun warn(message: String?) {
    LOG.warn(message)
  }
}