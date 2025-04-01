// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode.core

import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.V8ValuePromise
import java.util.concurrent.CompletableFuture

/**
 * Wrap CompletableFuture to JS Promise
 */
typealias Thenable = V8ValuePromise

object ThenableBuilder {
  fun <T> createCompleted(nodeRuntime: NodeRuntime, value: T): Thenable {
    val promise = nodeRuntime.createV8ValuePromise()
    promise.resolve(value)
    return promise.promise
  }

  fun <T> create(nodeRuntime: NodeRuntime, completableFuture: CompletableFuture<T>): Thenable {
    val promise = nodeRuntime.createV8ValuePromise()
    completableFuture.whenComplete { result, error ->
      if (error != null) {
        promise.reject(error.message ?: "Unknown error")
      }
      else {
        promise.resolve(result)
      }
    }
    return promise.promise
  }

}