// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal

import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.V8Value
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.Uri
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.ui.TerminalWidget
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.jetbrains.plugins.terminal.TerminalTabState
import org.jetbrains.plugins.terminal.TerminalToolWindowManager

private val LOG = Logger.getInstance("#cline.terminal")

class Terminal(
  private val project: Project,
  private val nodeRuntime: NodeRuntime,
  creationOptions: TerminalOptions,
) {
  //val name: String
  //val processId: Long
  //val creationOptions: TerminalOptions
  //val exitStatus: TerminalExitStatus
  //   readonly state: TerminalState;
  private var shellIntegration: TerminalShellIntegration? = null
  private var widget: TerminalWidget? = null

  var exitStatus: TerminalExitStatus? = null

  init {
    TerminalOutputListenerRegistry.getInstance(project)
    MainScope().plus(CoroutineName("Terminal")).launch {
      edtWriteAction {
        val manager = TerminalToolWindowManager.getInstance(project)
        val tabState = TerminalTabState()
        tabState.myTabName = creationOptions.name
        tabState.myWorkingDirectory = creationOptions.cwd
        val runner = TerminalRunner(project)
        widget = manager.createNewSession(runner, tabState)
        shellIntegration = TerminalShellIntegration(project, nodeRuntime, widget!!, Uri(creationOptions.cwd))
        widget!!.addTerminationCallback(Runnable {
          LOG.info("widget terminated")
          exitStatus = TerminalExitStatus(0)
        }, widget!!)
      }
    }
  }

  fun getShellIntegration(): V8Value {
    LOG.info("getShellIntegration $shellIntegration")
    if (shellIntegration == null) {
      return nodeRuntime.createV8ValueUndefined()
    }
    else {
      return nodeRuntime.converter.toV8Value(nodeRuntime, shellIntegration)
    }
  }

  fun getExitStatus(): V8Value {
    if (exitStatus == null) {
      return nodeRuntime.createV8ValueUndefined()
    }
    return nodeRuntime.converter.toV8Value(nodeRuntime, exitStatus)
  }

  fun sendText(text: String, addNewLine: Boolean?) {
    LOG.info("sendText text=$text addNewLine=$addNewLine")
    widget?.writePlainMessage(text)
    if (addNewLine == true) {
      widget?.writePlainMessage("\n")
    }
  }

  fun show(preserveFocus: Boolean?) {
    LOG.info("show preserveFocus=$preserveFocus")
    widget?.preferredFocusableComponent?.isVisible = true
    if (preserveFocus == true) {
      widget?.requestFocus()
    }
  }

  fun hide() {
    LOG.info("hide")
    widget?.preferredFocusableComponent?.isVisible = false
  }

  fun dispose() {
    LOG.info("dispose")
    widget?.let {
      Disposer.dispose(widget!!)
    }
  }
}