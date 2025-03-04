// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.vscode

import com.caoccao.javet.values.V8Value

data class Tab(
  val label: String? = null,
  val input: TabInputText? = null,
  val group: V8Value? = null,
  val isActive: Boolean = false,
  val isDirty: Boolean = false,
  val isPinned: Boolean = false,
  val isPreview: Boolean = false,
)
