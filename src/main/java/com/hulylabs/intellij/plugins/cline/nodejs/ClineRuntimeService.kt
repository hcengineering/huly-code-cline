// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs

import com.caoccao.javet.enums.V8AwaitMode
import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.exceptions.JavetExecutionException
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.converters.JavetProxyConverter
import com.caoccao.javet.interop.options.NodeRuntimeOptions
import com.caoccao.javet.node.modules.NodeModuleAny
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.node.modules.NodeModuleProcess
import com.caoccao.javet.values.reference.V8ValueFunction
import com.caoccao.javet.values.reference.V8ValueObject
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.WebviewView
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.io.path.exists

@Service(Service.Level.PROJECT)
class ClineRuntimeService(
  private val project: Project,
  private val scope: CoroutineScope,
) : Disposable {

  private val started = AtomicBoolean(false)
  private val runtimePath = PathManager.getSystemDir().resolve("cline-runtime")
  private val logger = ClineRuntimeLogger()

  private lateinit var nodeRuntime: NodeRuntime
  private lateinit var moduleObject: V8ValueObject
  private lateinit var thread: Thread
  private val messageQueue = ArrayBlockingQueue<Pair<V8ValueFunction, String>>(100)

  companion object {
    fun getInstance(project: Project): ClineRuntimeService {
      return project.getService(ClineRuntimeService::class.java)
    }
  }

  fun activate(browser: JBCefBrowser) {
    logger.info("Init runtime service")
    scope.launch {
      prepareRuntime()
      thread = startRuntime(project, browser)
    }
  }

  @Throws(IOException::class)
  private suspend fun prepareRuntime() {
    withContext(Dispatchers.IO) {
      if (!runtimePath.exists()) {
        runtimePath.toFile().mkdirs()
      }
      for (file in runtimePath.toFile().listFiles()!!) {
        file.delete()
      }
      val clazz = ClineRuntimeService::class.java
      val rootRuntimeDir = VfsUtil.findFileByURL(clazz.getResource("/nodejs/runtime")!!)!!

      for (file in rootRuntimeDir.children) {
        Files.copy(file.inputStream, runtimePath.resolve(file.name))
      }
      Files.copy(VfsUtil.findFileByURL(clazz.getResource("/nodejs/icudata/icudtl.dat")!!)!!.inputStream,
                 runtimePath.resolve("icudtl.dat"))

      val rootThemesDir = VfsUtil.findFileByURL(clazz.getResource("/vscode-themes")!!)!!

      for (file in rootThemesDir.children) {
        Files.copy(file.inputStream, runtimePath.resolve(file.name))
      }
      RipGrepRuntime.init()
    }
  }

  private fun startRuntime(project: Project, browser: JBCefBrowser): Thread {
    return thread {
      started.set(true)
      try {
        NodeRuntimeOptions.NODE_FLAGS.setIcuDataDir(runtimePath.toString())
        nodeRuntime = V8Host.getNodeI18nInstance().createV8Runtime<NodeRuntime>()
        val bridge = HulyCodeBridge(project, nodeRuntime)
        val webView = WebviewView(project, nodeRuntime, browser)
        nodeRuntime.logger = logger
        try {
          nodeRuntime.getNodeModule(NodeModuleModule::class.java).setRequireRootDirectory(runtimePath.toFile())
          nodeRuntime.getNodeModule(NodeModuleProcess::class.java).workingDirectory = runtimePath.toFile()
          val javetConverter = JavetProxyConverter()
          nodeRuntime.converter = javetConverter
          nodeRuntime.globalObject.set("hulyCode", bridge)
          nodeRuntime.globalObject.set("webview", webView)
          val module: NodeModuleAny? = nodeRuntime.getNodeModule<NodeModuleAny?>("./cline.js", NodeModuleAny::class.java)
          moduleObject = module!!.moduleObject
          moduleObject.setWeak()
          moduleObject.invokeVoid("activate", emptyArray<Object>())
          while (started.get()) {
            nodeRuntime.await(V8AwaitMode.RunOnce)
            Thread.sleep(10)
            val message = messageQueue.poll(10, TimeUnit.MILLISECONDS)
            if (message != null) {
              val (function, message) = message
              try {
                moduleObject.invokeVoid("invokeCallback", function, message)
              }
              catch (e: JavetException) {
                e.printStackTrace()
              }
            }
          }
        }
        catch (ex: JavetExecutionException) {
          val err = ex.scriptingError
          logger.error("${err.detailedMessage}:\n ${err.stack}")
        }
        catch (ex: JavetException) {
          ex.printStackTrace()
        }
        logger.info("Destroy runtime service")
        nodeRuntime.globalObject.delete("hulyCode")
        nodeRuntime.globalObject.delete("webview-cline")
        moduleObject.close()
        Disposer.dispose(bridge)
        Disposer.dispose(webView)
      }
      catch (e: Exception) {
        logger.error("Cannot start cline runtime", e)
      }
    }
  }

  override fun dispose() {
    started.set(false)
    thread.join(1000)
  }

  fun addMessage(onDidReceiveMessageListener: V8ValueFunction, json: String) {
    messageQueue.offer(Pair(onDidReceiveMessageListener, json))
  }
}