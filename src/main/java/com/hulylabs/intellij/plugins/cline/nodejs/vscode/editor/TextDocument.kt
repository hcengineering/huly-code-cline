// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor

import com.caoccao.javet.annotations.V8Property
import com.caoccao.javet.interop.NodeRuntime
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Position
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Range
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Thenable
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.ThenableBuilder
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Uri
import com.intellij.openapi.application.ReadAction.compute
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile

private val LOG = Logger.getInstance("#cline.text_document")
open class TextDocument(
  private val nodeRuntime: NodeRuntime,
  open val editor: FileEditor,
) {

  open fun getEditorFile(): VirtualFile {
    return editor.file
  }

  fun getDocument(): Document? {
    return compute<Document, Exception> {
      FileDocumentManager.getInstance().getDocument(getEditorFile())
    }
  }

  fun getUri(): Uri {
    return Uri(getEditorFile().path)
  }

  fun getLanguageId(): String {
    // TODO: detect language ID from extension because it always TreeSetter language file
    return if (getEditorFile().fileType is LanguageFileType) {
      (getEditorFile().fileType as LanguageFileType).language.id
    }
    else {
      "plaintext"
    }
  }

  @V8Property(name = "isDirty")
  open fun getIsDirty(): Boolean {
    return editor.isModified
  }

  fun getLineCount(): Int {
    return getDocument()?.lineCount ?: 0
  }

  open fun save(): Thenable/*<Boolean>*/ {
    try {
      getDocument()?.let {
        FileDocumentManager.getInstance().saveDocument(getDocument()!!)
        getEditorFile().refresh(true, true)
      }
      return ThenableBuilder.createCompleted(nodeRuntime, nodeRuntime.createV8ValueBoolean(true))
    }
    catch (_: Exception) {
      return ThenableBuilder.createCompleted(nodeRuntime, nodeRuntime.createV8ValueBoolean(false))
    }
  }

  fun positionAt(offset: Int): Position {
    val document = getDocument()
    if (document != null) {
      val line = document.getLineNumber(offset)
      return Position(line, offset - document.getLineStartOffset(line))
    }
    return Position(0, 0)
  }

  private fun getTextRange(range: Range): TextRange {
    val document = getDocument()
    if (document == null) {
      return TextRange.EMPTY_RANGE
    }
    val startOffset = document.getLineNumber(range.start.line) + range.start.column
    val endOffset = document.getLineNumber(range.end.line) + range.end.column
    return TextRange(startOffset, endOffset)
  }

  fun getText(range: Map<String, Any>): String {
    val range = Range.fromMap(range)
    LOG.info("getText $range")
    return getDocument()?.getText(getTextRange(range)) ?: ""
  }

  fun getText(): String {
    LOG.info("getAllText")
    return getDocument()?.text ?: ""
  }
}