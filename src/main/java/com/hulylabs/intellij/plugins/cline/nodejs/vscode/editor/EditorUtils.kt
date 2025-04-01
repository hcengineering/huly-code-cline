// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor

import java.util.Base64

object EditorUtils {
  /**
   * Try to represent given string as uri and get content from queue part as base64 encoded string.
   */
  fun String.getQueueContent(): String {
    val idx = this.lastIndexOf('?')
    if (idx != -1) {
      val queue = this.substring(idx + 1)
      return Base64.getDecoder().decode(queue).toString(Charsets.UTF_8)
    }
    return ""
  }
}