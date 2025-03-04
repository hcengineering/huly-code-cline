// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser

class NewTaskAction(browser: JBCefBrowser) : BaseClineAction(browser, "New Task", "add.svg") {
  override fun getAction(): String = "chatButtonClicked"

  override fun actionPerformed(e: AnActionEvent) {
    super.actionPerformed(e)
  }
}