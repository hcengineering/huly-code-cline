// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.core

data class Selection(
  val selectionStartLineNumber: Int,
  val selectionStartColumn: Int,
  val positionLineNumber: Int,
  val positionColumn: Int,
) {
  companion object {
    fun Any?.smartToInt(): Int {
      return if (this is Double) (Int.MAX_VALUE / 2) else (this as Int)
    }

    @JvmStatic
    fun fromMap(map: Map<String, Any>): Selection {
      return Selection(
        map["selectionStartLineNumber"].smartToInt(),
        map["selectionStartColumn"].smartToInt(),
        map["positionLineNumber"].smartToInt(),
        map["positionColumn"].smartToInt(),
      )
    }
  }
}
