// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal

import java.util.EventListener

interface TerminalOutputListener: EventListener {
  fun onOutput(output: String)
  fun onStart()
  fun onFinish(exitCode: Int)
}