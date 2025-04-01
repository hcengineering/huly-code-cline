// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.core

import com.caoccao.javet.values.reference.V8ValueReference
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

class JsDisposable(
  /** NodeRuntime value to dispose when JS dispose is called */
  private val value: V8ValueReference? = null,
  /** IDEA Disposable to dispose when JS dispose is called */
  val disposable: Disposable? = null,
) {
  fun dispose() {
    if (value?.isClosed != true) {
      value?.close(true)
    }
    println("JsDisposable.dispose")
    disposable?.let {
      Disposer.dispose(disposable)
    }
  }
}