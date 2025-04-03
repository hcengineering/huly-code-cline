// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.settings

enum class ReasoningEffort(val displayName: String) {
  LOW("low"),
  MEDIUM("medium"),
  HIGH("high");

  companion object {
    fun forName(name: String) = ReasoningEffort.entries.find{ it.displayName == name }
  }
}