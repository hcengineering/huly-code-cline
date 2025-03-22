// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode

data class Range(
  val startLineNumber: Int,
  val startColumn: Int,
  val endLineNumber: Int,
  val endColumn: Int,
  val start: Position,
  val end: Position,
)
