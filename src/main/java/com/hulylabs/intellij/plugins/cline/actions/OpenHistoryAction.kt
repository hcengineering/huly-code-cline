// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.actions

import com.intellij.ui.jcef.JBCefBrowser

class OpenHistoryAction(browser: JBCefBrowser) : BaseClineAction(browser, "History", "history.svg") {
  override fun getAction(): String = "historyButtonClicked"
}