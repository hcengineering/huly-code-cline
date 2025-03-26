// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal

data class TerminalExitStatus(
  val reason: Int,
  val code: Int? = null,
)