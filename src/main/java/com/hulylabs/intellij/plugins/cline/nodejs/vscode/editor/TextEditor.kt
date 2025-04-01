// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor

import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.V8Value
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditor

private val LOG = Logger.getInstance("#cline.editor")

@Suppress("unused")
open class TextEditor(
  private val nodeRuntime: NodeRuntime,
  private val editor: FileEditor,
) {

  open fun getDocument(): TextDocument {
    LOG.info("getDocument")
    return TextDocument(nodeRuntime, editor)
  }

  fun setSelection(selection: Map<String, Any>) {
    LOG.info("setSelection $selection")
    println("setSelection $selection")
  }


  fun getOptions(): TextEditorOptions {
    LOG.info("getOptions")
    return TextEditorOptions()
  }

  fun getViewColumn(): V8Value? {
    LOG.info("getViewColumn")
    //readonly viewColumn: ViewColumn | undefined;
    return null
  }

  open fun setDecorations(decorationType: Map<String, Any>, rangesOrOptions: List<Map<String, Any>>) {
    LOG.info("setDecorations $rangesOrOptions")
  }

  open fun revealRange(range: Map<String, Any>, revealType: Int?) {
    LOG.info("revealRange $range")
  }

  //show(column?: ViewColumn): void;

  //hide(): void;
}