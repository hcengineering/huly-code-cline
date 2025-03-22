// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.cdt

import com.caoccao.javet.interop.IV8InspectorListener
import com.caoccao.javet.interop.V8Runtime
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter


class CDTWebSocketAdapter(val v8Runtime: V8Runtime) : WebSocketAdapter(), IV8InspectorListener {

  override fun flushProtocolNotifications() {
  }

  override fun onWebSocketClose(statusCode: Int, reason: String?) {
    println("onWebSocketClose $statusCode $reason")
    v8Runtime.getV8Inspector().removeListeners(this)
    super.onWebSocketClose(statusCode, reason)
  }

  override fun onWebSocketConnect(sess: Session?) {
    println("onWebSocketConnect")
    super.onWebSocketConnect(sess)
    v8Runtime.getV8Inspector().addListeners(this)
  }

  override fun onWebSocketError(cause: Throwable) {
    cause.printStackTrace()
  }

  override fun onWebSocketText(message: String?) {
    println("onWebSocketText $message")
    try {
      v8Runtime.getV8Inspector().sendRequest(message)
    }
    catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun receiveNotification(message: String?) {
    println("receiveNotification $message")
    try {
      remote.sendString(message)
    }
    catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun receiveResponse(message: String?) {
    println("receiveResponse $message")
    try {
      remote.sendString(message)
    }
    catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun runIfWaitingForDebugger(contextGroupId: Int) {
    println("runIfWaitingForDebugger")
    try {
      v8Runtime.getExecutor(
        "console.log('Welcome to Javet Debugging Environment!');").executeVoid()
    }
    catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun sendRequest(message: String?) {
  }
}