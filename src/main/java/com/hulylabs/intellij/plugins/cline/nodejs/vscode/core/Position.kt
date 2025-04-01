// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.core

@Suppress("unused")
data class Position(
  val line: Int,
  val column: Int,
) {
  companion object {
    fun Any?.smartToInt(): Int {
      return if (this is Double) (Int.MAX_VALUE / 2) else (this as Int)
    }

    @JvmStatic
    fun fromMap(map: Map<String, Any>): Position {
      return Position(
        map["line"].smartToInt(),
        map["column"].smartToInt()
      )
    }
  }
}