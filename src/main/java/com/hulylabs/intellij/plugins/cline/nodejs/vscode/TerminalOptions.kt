// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode


class TerminalOptions {
  var name: String? = null

  //val shellArgs: List<String>?,
  var cwd: String? = null
  //val env: Map<String, String>,
  //val strictEnv: Boolean?,
  //val hideFromUser: Boolean?,
  //val message: String?,
  //val iconPath: ThemeIcon?,

  companion object {
    fun fromMap(map: Map<Any, Any>): TerminalOptions {
      var options = TerminalOptions()
      options.name = map["name"] as String?
      options.cwd = map["cwd"] as String?
      return options
    }
  }
}

