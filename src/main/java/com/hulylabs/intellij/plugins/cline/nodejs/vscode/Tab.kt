// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode

data class Tab(
  val path: String,
  val isDirty: Boolean,
  val isDiff: Boolean,
)