// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.jcef.JBCefBrowser

abstract class BaseClineAction(val browser: JBCefBrowser, name: String, icon: String)
  : AnAction(name, null, IconLoader.getIcon("/icons/${icon}", BaseClineAction::class.java)) {

  abstract fun getAction(): String

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    browser.cefBrowser.executeJavaScript(
      """window.postMessage({type: "action", action: "${getAction()}"}, "*");""",
      browser.cefBrowser.url,
      0
    )
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }
}