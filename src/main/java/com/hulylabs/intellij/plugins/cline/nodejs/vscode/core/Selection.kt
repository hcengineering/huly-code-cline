// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.core

data class Selection(
  val selectionStartLineNumber: Int,
  val selectionStartColumn: Int,
  val positionLineNumber: Int,
  val positionColumn: Int,
)