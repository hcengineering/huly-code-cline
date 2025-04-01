// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.core

@Suppress("unused")
data class Range(
  val startLineNumber: Int,
  val startColumn: Int,
  val endLineNumber: Int,
  val endColumn: Int,
  val start: Position,
  val end: Position,
) {
  companion object {
    fun Any?.smartToInt(): Int {
      return if (this is Double) (Int.MAX_VALUE / 2) else (this as Int)
    }

    @JvmStatic
    fun fromMap(map: Map<String, Any>): Range {
      return Range(map["startLineNumber"].smartToInt(),
                   map["startColumn"].smartToInt(),
                   map["endLineNumber"].smartToInt(),
                   map["endColumn"].smartToInt(),
                   @Suppress("UNCHECKED_CAST")
                   Position.fromMap(map["start"] as Map<String, Any>),
                   @Suppress("UNCHECKED_CAST")
                   Position.fromMap(map["end"] as Map<String, Any>))
    }
  }
}