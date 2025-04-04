// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.core

@Suppress("unused")
class Uri(val fsPath: String?) {
  fun getFilePath(): String? {
    return fsPath
  }

  override fun toString(): String {
    return "Uri(${fsPath ?: ""})"
  }
}