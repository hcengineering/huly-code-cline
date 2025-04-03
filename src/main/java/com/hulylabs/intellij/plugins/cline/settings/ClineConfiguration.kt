// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

@State(name = "ClineConfiguration", storages = [Storage("Cline.xml")])
@Service
class ClineConfiguration : SimplePersistentStateComponent<ClineConfiguration.State>(State()) {
  class State : BaseState() {
    var globalParams by map<String, String>()
    var workspaceParams by map<String, String>()
  }

  var globalParams
    get() = state.globalParams
    set(value) {
      if (state.globalParams != value) {
        state.globalParams = value
      }
    }

  var workspaceParams
    get() = state.workspaceParams
    set(value) {
      if (state.workspaceParams != value) {
        state.workspaceParams = value
      }
    }

  fun get(key: String): String {
    val value = state.workspaceParams[key]
    if (value != null) {
      return value
    }
    return when (key) {
      "cline.chromeExecutablePath" -> ""
      "cline.enableCheckpoints" -> "true"
      "cline.disableBrowserTool" -> "false"
      "cline.mcp.mode" -> "full"
      "cline.mcpMarketplace.enabled" -> "true"
      "cline.modelSettings.o3Mini.reasoningEffort" -> "medium"
      "cline.preferredLanguage" -> "English"
      else -> ""
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(): ClineConfiguration = service()
  }
}