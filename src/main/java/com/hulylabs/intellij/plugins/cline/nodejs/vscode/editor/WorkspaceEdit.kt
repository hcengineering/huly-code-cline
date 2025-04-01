// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor

import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Range
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.Uri

data class WorkspaceEdit(
  val uri: Uri,
  val range: Range,
  val newText: String? = null,
) {
  companion object {
    @JvmStatic
    fun fromMap(map: Map<String, Any>): WorkspaceEdit {
      return WorkspaceEdit(map["uri"] as Uri,
                           @Suppress("UNCHECKED_CAST")
                           Range.fromMap(map["range"] as Map<String, Any>),
                           map["newText"] as String?)
    }
  }
}