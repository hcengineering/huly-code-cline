// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.ui.TerminalWidget
import com.intellij.util.EventDispatcher
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import org.jetbrains.plugins.terminal.block.BlockTerminalInitializationListener
import org.jetbrains.plugins.terminal.block.output.CommandBlock
import org.jetbrains.plugins.terminal.block.output.CommandBlockInfo
import org.jetbrains.plugins.terminal.block.output.TerminalOutputModel
import org.jetbrains.plugins.terminal.block.output.TerminalOutputModelListener
import org.jetbrains.plugins.terminal.block.prompt.TerminalPromptModel
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.math.max

@Service(Service.Level.PROJECT)
class TerminalOutputListenerRegistry(project: Project) {

  private val eventDispatchers = mutableMapOf<TerminalWidget, EventDispatcher<TerminalOutputListener>>()
  private val lastWidgetRef = AtomicReference<TerminalWidget?>()

  init {
    TerminalToolWindowManager.getInstance(project).addNewTerminalSetupHandler(Consumer { widget ->
      lastWidgetRef.set(widget)
      eventDispatchers[widget] = EventDispatcher.create(TerminalOutputListener::class.java)
    }, Disposer.newDisposable())
    project.messageBus.connect().subscribe(BlockTerminalInitializationListener.TOPIC, object : BlockTerminalInitializationListener {
      override fun modelsInitialized(promptModel: TerminalPromptModel, outputModel: TerminalOutputModel) {
        var output = ""
        var lastLineIdx = 0
        var lastOffset = 0
        val widget = lastWidgetRef.getAndSet(null)
        outputModel.editor.document.addDocumentListener(object : DocumentListener {
          override fun documentChanged(event: DocumentEvent) {
            val offset = max(0, event.offset - lastOffset)
            if (event.oldFragment.isNotEmpty()) {
              if (event.newFragment.isNotEmpty()) {
                output = output.substring(0, offset) + event.newFragment + output.substring(offset + event.oldFragment.length)
              }
              else {
                output = output.substring(0, offset) + output.substring(offset + event.oldFragment.length)
              }
            }
            else {
              output = output.substring(0, offset) + event.newFragment + output.substring(offset)
            }
            val lines = output.split("\n")
            while (lastLineIdx < lines.size - 5) {
              eventDispatchers[widget]?.multicaster?.onOutput(lines[lastLineIdx++] + "\n")
            }
          }
        })
        outputModel.addListener(object : TerminalOutputModelListener {
          override fun blockCreated(block: CommandBlock) {
            output = ""
            lastOffset = block.outputStartOffset
            lastLineIdx = 0
            if (block.command != null) {
              eventDispatchers[widget]?.multicaster?.onStart()
            }
          }

          override fun blockFinalized(block: CommandBlock) {
          }

          override fun blockInfoUpdated(block: CommandBlock, newInfo: CommandBlockInfo) {
            if (block.command != null) {
              val lines = output.split("\n")
              while (lastLineIdx < lines.size) {
                eventDispatchers[widget]?.multicaster?.onOutput(lines[lastLineIdx++] + "\n")
              }
              eventDispatchers[widget]?.multicaster?.onFinish(newInfo.exitCode)
            }
          }
        })
      }
    })
  }

  fun addListener(listener: TerminalOutputListener, widget: TerminalWidget) {
    eventDispatchers[widget]?.addListener(listener)
  }

  fun removeListener(listener: TerminalOutputListener, widget: TerminalWidget) {
    eventDispatchers[widget]?.removeListener(listener)
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): TerminalOutputListenerRegistry = project.service()
  }
}