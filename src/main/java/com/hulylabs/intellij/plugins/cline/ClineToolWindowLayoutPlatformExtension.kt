// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline

import com.intellij.toolWindow.DefaultToolWindowLayoutBuilder
import com.intellij.toolWindow.DefaultToolWindowLayoutExtension

class ClineToolWindowLayoutPlatformExtension: DefaultToolWindowLayoutExtension {
  override fun buildV1Layout(builder: DefaultToolWindowLayoutBuilder) {
    // ignore
  }

  override fun buildV2Layout(builder: DefaultToolWindowLayoutBuilder) {
    builder.left.addOrUpdate("Cline") { weight = 0.1f }
  }
}