// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline

import com.caoccao.javet.annotations.V8Convert
import com.caoccao.javet.annotations.V8Function
import com.caoccao.javet.values.V8Value
import com.hulylabs.intellij.plugins.cline.vscode.Extension
import com.hulylabs.intellij.plugins.cline.vscode.Range
import com.hulylabs.intellij.plugins.cline.vscode.Tab
import com.hulylabs.intellij.plugins.cline.vscode.TabInputText
import com.hulylabs.intellij.plugins.cline.vscode.Terminal
import com.hulylabs.intellij.plugins.cline.vscode.TerminalOptions
import com.hulylabs.intellij.plugins.cline.vscode.Uri
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.util.ArrayUtil

private val LOG = Logger.getInstance("#cline")
private val CLINE_LOG = Logger.getInstance("#cline_log")

@Suppress("unused")
class HulyCodeBridge internal constructor(private val project: Project) : Disposable {
  fun log(message: String?) {
    CLINE_LOG.info(message)
  }

  //region Storage related
  val globalStoragePath: String
    get() {
      val path = PathManager.getConfigDir().resolve("cline")
      LOG.info("getGlobalStoragePath: " + path)
      return path.toString()
    }

  fun getSecret(key: String): String? {
    LOG.info("getSecret key=" + key)
    val attributes = CredentialAttributes(
      generateServiceName("Cline", key)
    )
    val passwordSafe = PasswordSafe.Companion.instance
    return passwordSafe.getPassword(attributes)
  }

  fun storeSecret(key: String, value: String?) {
    LOG.info("storeSecret key=" + key)
    val attributes = CredentialAttributes(
      generateServiceName("Cline", key)
    )
    val passwordSafe = PasswordSafe.Companion.instance
    val credentials = Credentials("", value)
    passwordSafe.set(attributes, credentials, false)
  }

  fun deleteSecret(key: String) {
    LOG.info("deleteSecret key=" + key)
    val passwordSafe = PasswordSafe.Companion.instance
    val attributes = CredentialAttributes(
      generateServiceName("Cline", key)
    )
    passwordSafe.set(attributes, null, false)
  }

  fun getConfiguration(section: String?, key: String?): String? {
    LOG.info("getConfiguration section=" + section + " key=" + key)
    if (section == "workbench") {
      if (key == "colorTheme") {
        val isDark = LafManager.getInstance().getCurrentUIThemeLookAndFeel().isDark
        if (isDark) {
          return "OneDark-Pro"
        }
        else {
          return "OneLight"
        }
      }
    }
    val fullKey = section + "." + key
    if (fullKey == "cline.enableCheckpoints") {
      return "true"
    }
    return ClineConfiguration.Companion.getInstance().workspaceParams.get(fullKey)
  }

  fun hasConfiguration(section: String?, key: String?): Boolean {
    LOG.info("hasConfiguration section=" + section + " key=" + key)
    return ClineConfiguration.Companion.getInstance().workspaceParams.containsKey(section + "." + key)
  }

  fun updateConfiguration(section: String?, key: String?, value: String?) {
    LOG.info("updateConfiguration section=" + section + " key=" + key + " value=" + value)
    ClineConfiguration.Companion.getInstance().workspaceParams.put(section + "." + key, value!!)
  }

  fun getExtension(extensionId: String?): Extension {
    LOG.info("getExtension extensionId=" + extensionId)
    return Extension(Uri(""))
  }

  fun globalStateKeys(): Array<String?> {
    LOG.info("globalStateKeys")
    return ArrayUtil.toStringArray(ClineConfiguration.Companion.getInstance().globalParams.keys)
  }

  fun globalStateGet(key: String?): String? {
    LOG.info("globalStateGet key=" + key)
    return ClineConfiguration.Companion.getInstance().globalParams.get(key)
  }

  fun globalStateUpdate(key: String?, value: String?) {
    LOG.info("globalStateUpdate key=" + key + " value=" + value)
    if (value == null) {
      ClineConfiguration.Companion.getInstance().globalParams.remove(key)
    }
    else {
      ClineConfiguration.Companion.getInstance().globalParams.put(key!!, value)
    }
  }

  //endregion
  fun workspaceFolders(): Array<String?> {
    LOG.info("workspaceFolders")
    return arrayOf<String?>(project.getBasePath())
  }

  fun getTabs(): List<Tab?> {
    LOG.info("getTabs")
    val fileEditors = FileEditorManager.getInstance(project).getOpenFiles()
    return fileEditors.map { file ->
      Tab(file!!.getName(),
          TabInputText(Uri(file.getPath())), null,
          true, false,
          false, false)
    }
  }

  fun getActiveTextEditor(): String? {
    LOG.info("getActiveTextEditor")
    val editor = FileEditorManager.getInstance(project).selectedEditor
    return editor?.file?.path
  }

  fun getVisibleTextEditors(): List<String> {
    LOG.info("getVisibleTextEditors")
    val editors = FileEditorManager.getInstance(project).allEditors
    return editors.filter{ it.isValid }.map { it.file.path }
  }

  fun setEditorDecorations(fsPath: String, key: String, rangesOrOptions: List<Range>) {
    LOG.info("setEditorDecorations fsPath=$fsPath key=$key rangesOrOptions=$rangesOrOptions")
  }

  fun createTerminal(options: HashMap<Any, Any>): Terminal? {
    val terminalOptions = TerminalOptions.fromMap(options)
    LOG.info("createTerminal options=$options")
    return Terminal("cline", 0, terminalOptions)
  }

  override fun dispose() {
  }
}