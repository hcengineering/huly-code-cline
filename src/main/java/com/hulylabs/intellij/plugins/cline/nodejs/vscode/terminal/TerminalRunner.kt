// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal

import com.intellij.openapi.project.Project
import org.jetbrains.plugins.terminal.LocalBlockTerminalRunner

/**
 * Terminal runner that uses the new terminal block UI, it is required to capture command output correctly.
 */
class TerminalRunner(project: Project): LocalBlockTerminalRunner(project) {
  override fun isGenOneTerminalEnabled(): Boolean {
    return true
  }

  override fun isGenTwoTerminalEnabled(): Boolean {
    return false
  }
}