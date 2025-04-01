// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal

import com.caoccao.javet.interop.NodeRuntime
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.AsyncIterable
import com.intellij.openapi.project.Project
import com.intellij.terminal.ui.TerminalWidget
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow

class TerminalCommandExecution(
  private val project: Project,
  private val nodeRuntime: NodeRuntime,
  private val widget: TerminalWidget,
) : TerminalOutputListener {
  var isFinished = false
  val channel = Channel<String>(Channel.UNLIMITED)
  fun read(): AsyncIterable<String> {
    return AsyncIterable(nodeRuntime, channel.consumeAsFlow())
  }

  override fun onOutput(output: String) {
    channel.trySend(output)
  }

  override fun onStart() {
  }

  override fun onFinish(exitCode: Int) {
    channel.close()
    TerminalOutputListenerRegistry.getInstance(project).removeListener(this, widget)
  }
}