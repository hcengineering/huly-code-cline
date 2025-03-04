// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline

import com.hulylabs.intellij.plugins.cline.actions.NewTaskAction
import com.hulylabs.intellij.plugins.cline.actions.OpenHistoryAction
import com.hulylabs.intellij.plugins.cline.actions.OpenMCPServersAction
import com.hulylabs.intellij.plugins.cline.actions.OpenSettingsAction
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefScrollbarsHelper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.cef.CefApp
import org.cef.callback.CefCallback
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

class ClineToolWindowFactory : ToolWindowFactory {

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val manager = toolWindow.contentManager
    val browser = JBCefBrowserBuilder().setEnableOpenDevToolsMenuItem(true).build()
    toolWindow.setTitleActions(listOf(NewTaskAction(browser), OpenMCPServersAction(browser), OpenHistoryAction(browser), OpenSettingsAction(browser)))
    browser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 10)
    CefApp.getInstance().registerSchemeHandlerFactory("http", "hulycline")
    { _, _, _, _ -> CustomResourceHandler() }
    //browser.openDevtools()
    val clineRuntimeService = ClineRuntimeService.getInstance(project)
    clineRuntimeService.activate(browser)
    manager.addContent(manager.factory.createContent(browser.component, null, true).apply { isCloseable = false })
    Disposer.register(toolWindow.disposable, clineRuntimeService)
  }
}

private const val STATIC_URL_PREFIX = "http://hulycline/"

class CustomResourceHandler : CefResourceHandler {
  private var myInputStream: InputStream? = null
  private var myMimeType: String? = null

  override fun processRequest(request: CefRequest?, callback: CefCallback?): Boolean {
    val url = request?.url
    if (url != null) {
      val path = url.replace(STATIC_URL_PREFIX, "")
      val ext = path.substringAfterLast(".", "txt")
      when (ext) {
        "html" -> myMimeType = "text/html"
        "js" -> myMimeType = "text/javascript"
        "css" -> myMimeType = "text/css"
        "map" -> myMimeType = "application/json"
        "ttf" -> myMimeType = "font/ttf"
        else -> myMimeType = "text/plain"
      }
      if (path == "vscode.css") {
        val css = generateVsCodeCss()
        myInputStream = css.byteInputStream()
        callback?.Continue()
        return true
      }
      else {
        val path = url.replace("http://hulycline", "/webview")
        val resource = CustomResourceHandler::class.java.getResource(path)
        if (resource != null) {
          myInputStream = resource.openStream()
          callback?.Continue()
          return true
        }
      }
    }
    callback?.cancel()
    return false
  }

  override fun getResponseHeaders(response: CefResponse?, responseLength: IntRef?, redirectUrl: StringRef?) {
    response?.mimeType = myMimeType
    response?.status = 200
  }

  override fun readResponse(dataOut: ByteArray?, bytesToRead: Int, bytesRead: IntRef?, callback: CefCallback?): Boolean {
    try {
      val availableSize = myInputStream!!.available()
      if (availableSize > 0) {
        var bytesToRead = min(bytesToRead.toDouble(), availableSize.toDouble()).toInt()
        bytesToRead = myInputStream!!.read(dataOut, 0, bytesToRead)
        bytesRead?.set(bytesToRead)
        return true
      }
    }
    catch (e: IOException) {
      e.printStackTrace()
    }
    bytesRead?.set(0)
    try {
      myInputStream!!.close()
    }
    catch (e: IOException) {
      e.printStackTrace()
    }
    return false
  }

  override fun cancel() {
  }

  fun generateVsCodeCss(): String {
    val isDark = LafManager.getInstance().getCurrentUIThemeLookAndFeel().isDark
    val themePath = "/vscode-themes/${if (isDark) "OneDark-Pro" else "OneLight"}.json"
    var themeContent = CustomResourceHandler::class.java.getResource(themePath)!!.readText()
    var keys = mutableSetOf<String>()
    val css = convertVsCodeTheme(themeContent, keys)
    return ":root {\n $css ${JBCefScrollbarsHelper.buildScrollbarsStyle()} \n }"
  }

  fun convertVsCodeTheme(themeContent: String, keys: MutableSet<String>): String {
    var result = ""
    var obj = Json.parseToJsonElement(themeContent).jsonObject
    if (obj.containsKey("colors")) {
      val colors = obj["colors"]!!.jsonObject
      for (key in colors.keys) {
        if (!keys.contains(key)) {
          keys.add(key)
        }
        else {
          continue
        }
        val value = colors[key]!!.jsonPrimitive.content
        if (key == "editor.foreground") {
          result += "  color: $value;\n"
        }
        else if (key == "editor.background") {
          result += "  background-color: $value;\n"
        }
        result += "  --vscode-${key.replaceFirst('.', '-')}: $value;\n"
      }
    }
    if (obj.containsKey("include")) {
      val include = obj["include"]!!.jsonPrimitive.content
      var themePath = "/vscode-themes/$include"
      var themeContent = CustomResourceHandler::class.java.getResource(themePath)!!.readText()
      result += convertVsCodeTheme(themeContent, keys)
    }
    return result
  }
}



