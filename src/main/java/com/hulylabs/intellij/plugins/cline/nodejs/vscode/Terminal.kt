// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode

import com.caoccao.javet.annotations.V8Convert
import com.caoccao.javet.enums.V8ProxyMode
import com.intellij.openapi.diagnostic.Logger

private val LOG = Logger.getInstance("#cline-terminal")

@V8Convert(proxyMode = V8ProxyMode.Class)
class Terminal {
  //val name: String
  //val processId: Long
  //val creationOptions: TerminalOptions
  //val exitStatus: TerminalExitStatus
  //   readonly state: TerminalState;

  constructor(name: String, processId: Long, creationOptions: TerminalOptions) {
    LOG.info("constructor name=$name processId=$processId creationOptions=$creationOptions")
  }

  fun sendText(text: String, addNewLine: Boolean?) {
    LOG.info("sendText text=$text addNewLine=$addNewLine")
  }

  fun show(preserveFocus: Boolean?) {
    LOG.info("show preserveFocus=$preserveFocus")
  }

  fun hide() {
    LOG.info("hide")
  }

  fun dispose() {
    LOG.info("dispose")
  }
}