// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.cdt

import com.hulylabs.intellij.plugins.cline.nodejs.cdt.CDTConfig.getWebSocketUrl
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException


class CDTHttpServlet : HttpServlet() {

  @Throws(ServletException::class, IOException::class)
  override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
    val requestURI = request.getRequestURI()
    response.setContentType("application/json; charset=UTF-8")
    response.setStatus(HttpServletResponse.SC_OK)
    if (CDTConfig.PATH_JSON == requestURI || CDTConfig.PATH_JSON_LIST == requestURI) {
      response.getWriter().println("[ {\n" +
                                   "  \"description\": \"javet\",\n" +
                                   "  \"devtoolsFrontendUrl\": \"devtools://devtools/bundled/js_app.html?experiments=true&v8only=true&ws=" + getWebSocketUrl() + "\",\n" +
                                   "  \"devtoolsFrontendUrlCompat\": \"devtools://devtools/bundled/inspector.html?experiments=true&v8only=true&ws=" + getWebSocketUrl() + "\",\n" +
                                   "  \"id\": \"javet\",\n" +
                                   "  \"title\": \"javet\",\n" +
                                   "  \"type\": \"node\",\n" +  // Type must be node
                                   "  \"url\": \"file://\",\n" +
                                   "  \"webSocketDebuggerUrl\": \"ws://" + getWebSocketUrl() + "\"\n" +
                                   "} ]\n")
    }
    else if (CDTConfig.PATH_JSON_VERSION == requestURI) {
      response.getWriter().println("{\n" +
                                   "  \"Browser\": \"Javet\",\n" +
                                   "  \"Protocol-Version\": \"1.3\"\n" +
                                   "} \n")
    }
    else {
      response.getWriter().println("{}")
    }
  }
}