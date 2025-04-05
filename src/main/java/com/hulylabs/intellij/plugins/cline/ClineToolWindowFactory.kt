// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline

import com.hulylabs.intellij.plugins.cline.actions.*
import com.hulylabs.intellij.plugins.cline.nodejs.ClineRuntimeService
import com.intellij.ide.BrowserUtil
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefScrollbarsHelper
import org.cef.CefApp
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefCallback
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.IOException
import java.io.InputStream
import javax.swing.UIManager
import kotlin.math.min

class ClineToolWindowFactory() : ToolWindowFactory {

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val manager = toolWindow.contentManager
    val browser = JBCefBrowserBuilder().setEnableOpenDevToolsMenuItem(true).build()
    toolWindow.setTitleActions(
      listOf(NewTaskAction(),
             OpenMCPServersAction(browser),
             OpenHistoryAction(browser),
             OpenAccountAction(browser),
             OpenSettingsAction(browser)
      ))
    browser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 100)
    browser.jbCefClient.addRequestHandler(object : CefRequestHandlerAdapter() {
      override fun onBeforeBrowse(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        userGesture: Boolean,
        isRedirect: Boolean,
      ): Boolean {
        val url = request?.url ?: return false

        if (!url.startsWith("http://hulycline/")) {
          BrowserUtil.browse(url)
          return true
        }
        return false
      }
    }, browser.cefBrowser)
    CefApp.getInstance().registerSchemeHandlerFactory("http", "hulycline")
    { _, _, _, _ -> CustomResourceHandler() }
    //browser.openDevtools()
    val clineRuntimeService = ClineRuntimeService.getInstance(project)
    clineRuntimeService.activate(browser)
    manager.addContent(manager.factory.createContent(browser.component, null, true).apply { isCloseable = false })
    Disposer.register(toolWindow.disposable, clineRuntimeService)
    registerUrlProtocol()
    ApplicationManager.getApplication().messageBus
      .connect(toolWindow.disposable)
      .subscribe(LafManagerListener.TOPIC, object : LafManagerListener {
        override fun lookAndFeelChanged(source: LafManager) {
          reloadStyles(browser)
        }
      })
  }

  private fun registerUrlProtocol() {
    if (!SystemInfo.isWindows) {
      return
    }

    try {
      val appPath = PathManager.getHomePath() + "\\bin\\huly-code64.exe"
      val scheme = "huly-code"
      var root = com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER
      if (!com.sun.jna.platform.win32.Advapi32Util.registryKeyExists(root, "SOFTWARE\\Classes\\$scheme")) {
        com.sun.jna.platform.win32.Advapi32Util.registryCreateKey(root, "SOFTWARE\\Classes\\$scheme")
        val protocolKey = com.sun.jna.platform.win32.Advapi32Util.registryGetKey(root, "SOFTWARE\\Classes\\$scheme", com.sun.jna.platform.win32.WinNT.KEY_WRITE)
        com.sun.jna.platform.win32.Advapi32Util.registrySetStringValue(protocolKey.value, "", "URL:$scheme")
        com.sun.jna.platform.win32.Advapi32Util.registrySetStringValue(protocolKey.value, "URL Protocol", "")
        com.sun.jna.platform.win32.Advapi32Util.registryCloseKey(protocolKey.value)

        com.sun.jna.platform.win32.Advapi32Util.registryCreateKey(root, "SOFTWARE\\Classes\\$scheme\\DefaultIcon")
        val iconKey = com.sun.jna.platform.win32.Advapi32Util.registryGetKey(root, "SOFTWARE\\Classes\\$scheme\\DefaultIcon", com.sun.jna.platform.win32.WinNT.KEY_WRITE)
        com.sun.jna.platform.win32.Advapi32Util.registrySetStringValue(iconKey.value, "", "$appPath,1")
        com.sun.jna.platform.win32.Advapi32Util.registryCloseKey(iconKey.value)
        com.sun.jna.platform.win32.Advapi32Util.registryCreateKey(root, "SOFTWARE\\Classes\\$scheme\\shell\\open\\command")
        val commandKey = com.sun.jna.platform.win32.Advapi32Util.registryGetKey(root, "SOFTWARE\\Classes\\$scheme\\shell\\open\\command", com.sun.jna.platform.win32.WinNT.KEY_WRITE)
        com.sun.jna.platform.win32.Advapi32Util.registrySetStringValue(commandKey.value, "", "\"$appPath\" \"%1\"")
        com.sun.jna.platform.win32.Advapi32Util.registryCloseKey(commandKey.value)
      }
    }
    catch (e: Exception) {
      Logger.getInstance("#cline.protocol").error(e)
    }
  }

  private fun reloadStyles(browser: JBCefBrowser) {
    browser.cefBrowser.executeJavaScript("""
      document.querySelectorAll("link[rel=stylesheet]").forEach(
        link => {
          link.href = link.href.replace(/\?.*|$\{'$'\}/, "?" + Date.now());
        }
      )
    """.trimIndent(), browser.cefBrowser.url, 0)
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
        val path = url.replace("http://hulycline", "/webview-cline")
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
    val font = UIManager.getFont("Panel.font")
    val themeCssName = if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) "dark" else "light"
    val themeColors = CustomResourceHandler::class.java.getResource("/vscode-themes/${themeCssName}.css")!!.readText()
    val scrollbarsStyle = JBCefScrollbarsHelper.buildScrollbarsStyle()
    return """:root {
      color: var(--vscode-foreground);
      --vscode-font-family: "${font.family}", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Noto Sans", "Ubuntu Sans", "Liberation Sans";
      --vscode-font-size: ${font.size}px;
      $themeColors
     }
     $scrollbarsStyle
     """
  }
}
