// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor

import com.caoccao.javet.interop.NodeRuntime
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Position
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Range
import com.intellij.diff.editor.DiffEditorViewerFileEditor
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*

private val LOG = Logger.getInstance("#cline.diff_editor")

class DiffTextEditor(
  private val project: Project,
  private val nodeRuntime: NodeRuntime,
  private val editor: DiffEditorViewerFileEditor,
) : TextEditor(nodeRuntime, editor) {
  val scope = MainScope().plus(CoroutineName("DiffTextEditor"))
  val highlights = mutableMapOf<Range, RangeHighlighter>()

  override fun getDocument(): TextDocument {
    LOG.info("getDiffDocument")
    return DiffTextDocument(project, nodeRuntime, editor)
  }

  fun Document.offset(position: Position): Int {
    if (position.line >= lineCount) {
      return textLength
    }
    val offset = getLineStartOffset(position.line) + position.column
    if (offset > textLength) {
      return textLength
    }
    return offset
  }

  fun applyEdit(edit: WorkspaceEdit) {
    LOG.info("applyEdit $edit")
    WriteCommandAction.writeCommandAction(project).run<Exception> {
      val document = editor.getEmbeddedEditors().last().document
      if (edit.newText != null) {
        document.replaceString(document.offset(edit.range.start), document.offset(edit.range.end), edit.newText)
      }
      else {
        document.deleteString(document.offset(edit.range.start), document.offset(edit.range.end))
      }
    }
  }

  override fun setDecorations(decorationType: Map<String, Any>, rangesOrOptions: List<Map<String, Any>>) {
    val ranges = rangesOrOptions.map { Range.fromMap(it) }
    var isFaded = decorationType["key"] as String? == "faded"
    LOG.info("setDecorations $isFaded $ranges")
    // TODO: implement custom decorations
    //for (editor in editor.getEmbeddedEditors()) {
    //  val document = editor.document
    //  for (range in ranges) {
    //    if (isFaded) {
    //      highlights[range] = editor.getMarkupModel().addRangeHighlighter(
    //        document.offset(range.start), document.offset(range.end),
    //        HighlighterLayer.SELECTION - 1,
    //        TextAttributes(JBColor.GRAY.darker(), null, null, null, 0),
    //        EXACT_RANGE
    //      )
    //    } else {
    //      highlights[range]?.let {
    //        editor.getMarkupModel().removeHighlighter(it)
    //      }
    //    }
    //  }
    //}
  }

  override fun revealRange(range: Map<String, Any>, revealType: Int?) {
    LOG.info("revealRange $range")
    val editor = editor.getEmbeddedEditors().last()
    val document = editor.document
    val range = Range.fromMap(range)
    scope.launch {
      withContext(Dispatchers.EDT) {
        editor.getScrollingModel().scrollTo(editor.offsetToLogicalPosition(document.offset(range.start)), ScrollType.MAKE_VISIBLE);
      }
    }
  }
}