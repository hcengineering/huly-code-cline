// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.cdt

import com.caoccao.javet.interop.NodeRuntime
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse
import org.eclipse.jetty.websocket.servlet.WebSocketCreator


class CDTWebSocketCreator(val nodeRuntime: NodeRuntime) : WebSocketCreator  {

  override fun createWebSocket(p0: ServletUpgradeRequest?, p1: ServletUpgradeResponse?): Any? {
    return CDTWebSocketAdapter(nodeRuntime)
  }

}