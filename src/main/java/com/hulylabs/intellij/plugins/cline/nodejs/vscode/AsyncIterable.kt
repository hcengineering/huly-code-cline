// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode

import com.caoccao.javet.enums.V8ValueSymbolType
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.callback.IJavetDirectCallable.NoThisAndResult
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.callback.JavetCallbackType
import com.caoccao.javet.interop.proxy.IJavetDirectProxyHandler
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueObject
import com.caoccao.javet.values.reference.V8ValuePromise
import com.caoccao.javet.values.reference.V8ValueSymbol
import com.caoccao.javet.values.reference.builtin.V8ValueBuiltInSymbol
import com.intellij.openapi.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow

/**
 * A wrapper for [Flow] that provides a JavaScript AsyncIterable interface.
 */
class AsyncIterable<T>(
  val nodeRuntime: NodeRuntime,
  flow: Flow<T>,
) : Disposable, IJavetDirectProxyHandler<Exception> {
  private val scope = MainScope().plus(CoroutineName("AsyncIterable"))
  private val queue = Channel<T>(Channel.UNLIMITED)
  private var isDone = false
  private var error: Throwable? = null

  init {
    scope.launch {
      try {
        flow.collect { item ->
          queue.send(item)
        }
      }
      catch (e: Throwable) {
        error = e
      }
      finally {
        isDone = true
        queue.close()
      }
    }
  }

  override fun getV8Runtime(): V8Runtime? {
    return nodeRuntime
  }

  override fun proxyGet(target: V8Value?, property: V8Value?, receiver: V8Value?): V8Value? {
    if (property is V8ValueSymbol) {
      if (V8ValueBuiltInSymbol.SYMBOL_PROPERTY_ASYNC_ITERATOR == property.description) {
        return nodeRuntime.createV8ValueFunction(
          JavetCallbackContext(
            V8ValueBuiltInSymbol.SYMBOL_PROPERTY_ASYNC_ITERATOR,
            V8ValueSymbolType.BuiltIn,
            JavetCallbackType.DirectCallNoThisAndResult,
            object : NoThisAndResult<Exception> {
              override fun call(v8Values: Array<V8Value?>?): V8Value? {
                return asyncIterator()
              }
            }
          )
        )
      }
    }
    return super.proxyGet(target, property, receiver)
  }

  fun next(): V8ValuePromise {
    val promise = nodeRuntime.createV8ValuePromise()

    scope.launch {
      try {
        val item = queue.receive()
        val result = nodeRuntime.createV8ValueObject()
        result.set("value", item)
        result.set("done", false)
        promise.resolve(result)
      }
      catch (e: Throwable) {
        when (e) {
          is ClosedReceiveChannelException,
          is CancellationException,
            -> {
            val result = nodeRuntime.createV8ValueObject()
            if (isDone) {
              result.set("done", true)
              promise.resolve(result)
            }
            else {
              promise.reject(e.message ?: "Unknown error")
            }
          }
          else -> {
            promise.reject(e.message ?: "Unknown error")
          }
        }
      }
    }

    return promise.promise
  }

  fun asyncIterator(): V8ValueObject {
    return nodeRuntime.createV8ValueObject().apply {
      set(
        "next",
        nodeRuntime.createV8ValueFunction(
          JavetCallbackContext(
            "next",
            V8ValueSymbolType.None,
            JavetCallbackType.DirectCallNoThisAndResult,
            object : NoThisAndResult<Exception> {
              override fun call(v8Values: Array<V8Value?>?): V8Value? {
                return next()
              }
            }
          )
        )
      )
    }
  }

  override fun dispose() {
    scope.cancel("Disposed")
    queue.close()
  }
}