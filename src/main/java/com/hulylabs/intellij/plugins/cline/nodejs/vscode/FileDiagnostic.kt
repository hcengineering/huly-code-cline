// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode

import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Range

data class FileDiagnostic(
  val file: String,
  val diagnostics: List<VsCodeDiagnostic>,
)

data class VsCodeDiagnostic(
  val range: Range,
  val message: String,
  val severity: Int,
  val source: String,
)