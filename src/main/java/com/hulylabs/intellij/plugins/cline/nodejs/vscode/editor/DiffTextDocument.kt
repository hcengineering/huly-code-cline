// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor

import com.caoccao.javet.interop.NodeRuntime
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Thenable
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.ThenableBuilder
import com.intellij.diff.editor.DiffEditorViewerFileEditor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.writeText
import java.util.concurrent.CompletableFuture

class DiffTextDocument(
  private val project: Project,
  private val nodeRuntime: NodeRuntime,
  override val editor: DiffEditorViewerFileEditor,
) : TextDocument(nodeRuntime, editor) {
  override fun getEditorFile(): VirtualFile {
    return editor.filesToRefresh.first()
  }

  override fun getIsDirty(): Boolean {
    val origDocument = editor.getEmbeddedEditors().first().document
    val diffDocument = editor.getEmbeddedEditors().last().document
    return origDocument.text != diffDocument.text
  }

  override fun save(): Thenable/*<Boolean>*/ {
    val diffDocument = editor.getEmbeddedEditors().last().document
    val result = CompletableFuture<Boolean>()
    WriteCommandAction.writeCommandAction(project).run<Exception> {
      val text = diffDocument.text
      val file = getEditorFile()
      file.writeText(text)
      result.complete(true)
    }
    return ThenableBuilder.create(nodeRuntime, result)
  }
}