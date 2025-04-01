// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal

import com.caoccao.javet.interop.NodeRuntime
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Uri
import com.intellij.openapi.project.Project
import com.intellij.terminal.ui.TerminalWidget

class TerminalShellIntegration(
  private val project: Project,
  private val nodeRuntime: NodeRuntime,
  private val widget: TerminalWidget,
  val cwd: Uri? = null,
) {

  fun executeCommand(command: String): TerminalCommandExecution {
    val execution = TerminalCommandExecution(project, nodeRuntime, widget)
    TerminalOutputListenerRegistry.getInstance(project).addListener(execution, widget)
    widget.sendCommandToExecute(command)
    return execution
  }
}