// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs

import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueFunction
import com.hulylabs.intellij.plugins.cline.settings.ClineConfiguration
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.Extension
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.FileDiagnostic
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.Tab
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.VsCodeDiagnostic
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.core.*
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor.DiffTextEditor
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor.EditorUtils.getQueueContent
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor.TextDocument
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor.TextEditor
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.editor.WorkspaceEdit
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal.Terminal
import com.hulylabs.intellij.plugins.cline.nodejs.vscode.terminal.TerminalOptions
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.editor.DiffEditorViewerFileEditor
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.ide.BrowserUtil
import com.intellij.ide.actions.OpenFileAction
import com.intellij.ide.actions.RevealFileAction
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.ui.LafManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.ex.ClipboardUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptorFactory
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.JBTerminalWidget
import com.intellij.util.ArrayUtil
import com.redhat.devtools.lsp4ij.features.diagnostics.LSPDiagnosticListener
import kotlinx.coroutines.*
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

private val LOG = Logger.getInstance("#cline")
private val CLINE_LOG = Logger.getInstance("#cline_log")

@Suppress("unused")
class HulyCodeBridge
internal constructor(
  private val project: Project,
  private val nodeRuntime: NodeRuntime,
) : Disposable {

  private val scope = MainScope().plus(CoroutineName("HulyCodeBridge"))
  private val diagnostics = mutableMapOf<String, List<Diagnostic>>()

  private var activeDiffEditor: DiffTextEditor? = null

  init {
    project.messageBus.connect(this).subscribe(LSPDiagnosticListener.TOPIC, object : LSPDiagnosticListener {
      override fun publishDiagnostics(params: PublishDiagnosticsParams) {
        val uri = params.uri
        val diagnostics = params.diagnostics
        this@HulyCodeBridge.diagnostics[uri] = diagnostics
      }
    })
  }

  fun getPluginVersion(): String {
    // TODO: Don`t forget to update this version according to Cline version or move it to properties file
    return "3.8.3"
  }

  fun log(message: String?) {
    CLINE_LOG.info(message)
  }

  //region Storage related
  val globalStoragePath: String
    get() {
      val path = PathManager.getSystemDir().resolve("cline")
      LOG.info("getGlobalStoragePath: $path")
      return path.toString()
    }

  fun getSecret(key: String): Thenable/*<String?>*/ {
    LOG.debug("getSecret key=$key")
    val attributes = CredentialAttributes(generateServiceName("Cline", key))
    val passwordSafe = PasswordSafe.Companion.instance
    val result = CompletableFuture<String?>()
    passwordSafe.getAsync(attributes).onSuccess { password ->
      result.complete(password?.password.toString())
    }.onError {  error ->
      LOG.error("getSecret error", error)
      result.completeExceptionally(error)
    }
    return ThenableBuilder.create(nodeRuntime, result)
  }

  fun storeSecret(key: String, value: String?): Thenable/*<void>*/ {
    LOG.debug("storeSecret key=$key")
    val attributes = CredentialAttributes(generateServiceName("Cline", key))
    val passwordSafe = PasswordSafe.Companion.instance
    val credentials = Credentials("", value)
    passwordSafe[attributes, credentials] = false
    return ThenableBuilder.createCompleted(nodeRuntime, Unit)
  }

  @JvmName("deleteSecret")
  fun deleteSecret(key: String): Thenable/*<void>*/ {
    LOG.debug("deleteSecret key=$key")
    val passwordSafe = PasswordSafe.Companion.instance
    val attributes = CredentialAttributes(generateServiceName("Cline", key))
    passwordSafe[attributes, null] = false
    return ThenableBuilder.createCompleted(nodeRuntime, Unit)
  }

  fun getConfiguration(section: String?, key: String?): String? {
    LOG.info("getConfiguration section=$section key=$key")
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
    val fullKey = "$section.$key"
    if (section?.startsWith("cline") == true) {
      // return with default value if not set
      return ClineConfiguration.Companion.getInstance().get(fullKey)
    }
    return ClineConfiguration.Companion.getInstance().workspaceParams[fullKey]
  }

  fun hasConfiguration(section: String?, key: String?): Boolean {
    LOG.info("hasConfiguration section=$section key=$key")
    return ClineConfiguration.Companion.getInstance().workspaceParams.containsKey("$section.$key")
  }

  fun updateConfiguration(section: String?, key: String?, value: String?, force: Boolean?) {
    LOG.info("updateConfiguration section=$section key=$key value=$value")
    ClineConfiguration.Companion.getInstance().workspaceParams.put("$section.$key", value!!)
  }

  fun getExtension(extensionId: String?): Extension {
    LOG.info("getExtension extensionId=$extensionId")
    return Extension(Uri(""))
  }

  fun globalStateKeys(): Array<String?> {
    LOG.debug("globalStateKeys")
    return ArrayUtil.toStringArray(ClineConfiguration.Companion.getInstance().globalParams.keys)
  }

  fun globalStateGet(key: String?): String? {
    LOG.debug("globalStateGet key=$key")
    return ClineConfiguration.Companion.getInstance().globalParams.get(key)
  }

  fun globalStateUpdate(key: String?, value: String?) {
    LOG.debug("globalStateUpdate key=$key value=$value")
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

  fun getTabs(): List<Tab> {
    LOG.info("getTabs")
    val fileEditors = FileEditorManager.getInstance(project).allEditors
    return fileEditors.map { editor ->
      if (editor is DiffEditorViewerFileEditor) {
        Tab(editor.filesToRefresh.first().path, editor.isModified, true)
      }
      else {
        Tab(editor.file.path, editor.isModified, false)
      }
    }
  }

  fun closeTab(filePath: String) {
    LOG.info("closeTab $filePath")
    val fileEditors = FileEditorManager.getInstance(project).allEditors
    scope.launch {
      withContext(Dispatchers.EDT) {
        fileEditors.forEach { editor ->
          val path = if (editor is DiffEditorViewerFileEditor) {
            editor.filesToRefresh.first().path
          }
          else {
            editor.file.path
          }
          if (path == filePath) {
            FileEditorManager.getInstance(project).closeFile(editor.file)
          }
        }
      }
    }
  }

  private fun toNodeEditor(editor: FileEditor): TextEditor {
    if (editor is DiffEditorViewerFileEditor) {
      return DiffTextEditor(project, nodeRuntime, editor)
    }
    return TextEditor(nodeRuntime, editor)
  }

  fun getActiveTextEditor(): TextEditor? {
    LOG.info("getActiveTextEditor")
    val editor = FileEditorManager.getInstance(project).selectedEditor
    return editor?.let { toNodeEditor(editor) }
  }

  fun getVisibleTextEditors(): List<TextEditor> {
    LOG.info("getVisibleTextEditors")
    val editors = FileEditorManager.getInstance(project).allEditors
    return editors.filter { it.isValid }.map { toNodeEditor(it) }
  }

  fun onDidChangeActiveTextEditor(listener: V8ValueFunction): JsDisposable {
    LOG.info("onDidChangeActiveTextEditor")
    val disposable = JsDisposable(listener, Disposer.newDisposable())
    listener.setWeak()
    nodeRuntime.globalObject.set("onDidChangeActiveTextEditor${listener.hashCode()}", listener)
    ApplicationManager.getApplication().messageBus.connect(disposable.disposable!!).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
      override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(file)
        if (editor != null) {
          var nodeEditor: TextEditor? = null
          if (editor is DiffEditorViewerFileEditor) {
            activeDiffEditor = DiffTextEditor(project, nodeRuntime, editor)
            nodeEditor = activeDiffEditor
          }
          else {
            nodeEditor = toNodeEditor(editor)
          }
          listener.callVoid(null, nodeEditor)
        }
      }
    })
    return disposable
  }

  fun createTerminal(options: HashMap<Any, Any>): Terminal? {
    val terminalOptions = TerminalOptions.Companion.fromMap(options)
    LOG.info("createTerminal options=$options")
    return Terminal(project, nodeRuntime, terminalOptions)
  }

  fun executeCommand(command: String, args: List<V8Value>): Thenable/*<void>*/ {
    LOG.info("executeCommand command=$command args=$args")
    when (command) {
      "vscode.diff" -> {
        val filePath = (args[1] as Map<*, *>)["filePath"].toString()
        val title = "${args[2]}"
        val file = VfsUtil.findFile(Path.of(filePath), true)
        scope.launch {
          withContext(Dispatchers.EDT) {
            val contentFactory = DiffContentFactory.getInstance()
            val diffRequest = SimpleDiffRequest(title, contentFactory.create(project, file!!), contentFactory.createEditable(project, file.readText(), file.fileType), filePath, "Cline changes")
            DiffManager.getInstance().showDiff(project, diffRequest)
          }
        }
      }
      "revealInExplorer" -> {
        val filePath = (args[1] as Map<*, *>)["filePath"].toString()
        RevealFileAction.openFile(Path.of(filePath))
      }
      "workbench.actions.view.problems" -> {
        ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.PROBLEMS_VIEW)?.show(null)
      }
      "workbench.action.terminal.focus" -> {
        ToolWindowManager.getInstance(project).getToolWindow("Terminal")?.show(null)
      }
      "workbench.action.openSettings" -> {

      }
      "claude-dev.SidebarProvider.focus" -> {
        ToolWindowManager.getInstance(project).getToolWindow("Cline")?.show(null)
      }
      "vscode.changes" -> {
        val title = "${args[0]}"
        val changes = args[1] as List<*>
        scope.launch {
          withContext(Dispatchers.EDT) {
            val diffRequests = mutableListOf<DiffRequest>()
            val contentFactory = DiffContentFactory.getInstance()
            for (change in changes) {
              val change = change as List<*>
              val absolutePath = (change[0] as Map<*, *>)["filePath"] as String
              val beforeContent = ((change[1] as Map<*, *>)["filePath"] as String).getQueueContent()
              val afterContent = ((change[2] as Map<*, *>)["filePath"] as String).getQueueContent()
              val file = VfsUtil.findFile(Path.of(absolutePath), true)
              val diffRequest = SimpleDiffRequest(file?.name, contentFactory.create(project, beforeContent, file), contentFactory.create(project, afterContent, file), file?.name
                                                                                                                                                                       ?: "Original", title)
              diffRequests.add(diffRequest)
            }
            val diffChain = SimpleDiffRequestChain(diffRequests)
            DiffManager.getInstance().showDiff(project, diffChain, DiffDialogHints.DEFAULT)
          }
        }
      }
      "setContext" -> {}
      "workbench.action.terminal.selectAll",
      "workbench.action.terminal.clearSelection",
        -> { // not used, we use copySelection instead to copy text of current terminal output
      }
      "workbench.action.terminal.copySelection" -> {
        val widget = TerminalToolWindowManager.getInstance(project).terminalWidgets.firstOrNull()
        if (widget != null) {
          val jediWidget = JBTerminalWidget.asJediTermWidget(widget)
          if (jediWidget != null) {
            CopyPasteManager.copyTextToClipboard(jediWidget.text)
          }
        }
      }
      "vscode.open" -> {
        val uri = "${args[0]}"
        OpenFileAction.openFile(uri, project)
      }
    }
    return ThenableBuilder.createCompleted(nodeRuntime, nodeRuntime.createV8ValueNull())
  }

  fun applyWorkspaceEdit(edit: Map<String, Any>): Thenable/*<boolean>*/ {
    val workspaceEdit = WorkspaceEdit.fromMap(edit)
    activeDiffEditor?.applyEdit(workspaceEdit)
    return ThenableBuilder.createCompleted(nodeRuntime, nodeRuntime.createV8ValueBoolean(true))
  }

  private fun openTextFile(uri: Map<String, Any>, needDocument: Boolean): Thenable {
    var filePath = uri["filePath"] as String
    var file = VfsUtil.findFile(Path.of(filePath), true)
    if (file != null) {
      val result = CompletableFuture<Any?>()
      scope.launch {
        withContext(Dispatchers.EDT) {
          val editor = FileEditorManager.getInstance(project).openFile(file, true).firstOrNull()
          if (editor != null) {
            result.complete(if (needDocument) TextDocument(nodeRuntime, editor) else TextEditor(nodeRuntime, editor))
          }
          else {
            result.complete(null)
          }
        }
      }
      return ThenableBuilder.create(nodeRuntime, result)
    }
    return ThenableBuilder.createCompleted(nodeRuntime, null)
  }

  fun openTextDocument(uri: Map<String, Any>): Thenable/*<TextDocument>*/ {
    LOG.info("openTextDocument $uri")
    return openTextFile(uri, true)
  }

  fun showTextDocument(uri: Map<String, Any>, options: Map<String, Any>?): Thenable/*<vscode.TextEditor>*/ {
    LOG.info("showTextDocument uri: $uri, options: $options")
    return openTextFile(uri, false)
  }

  fun showTextDocument(uri: Map<String, Any>): Thenable/*<vscode.TextEditor>*/ {
    return showTextDocument(uri, null)
  }

  fun showTextDocument(document: TextDocument, options: Map<String, Any>?): Thenable/*<vscode.TextEditor>*/ {
    LOG.info("showTextDocument document: $document, options: $options") // we assume that the document is already opened
    return ThenableBuilder.createCompleted(nodeRuntime, TextEditor(nodeRuntime, document.editor))
  }

  fun getDiagnostics(): List<FileDiagnostic> {
    LOG.info("getDiagnostics")
    return diagnostics.entries.map { (file, diagnostics) ->
        FileDiagnostic(file, diagnostics.map {
          VsCodeDiagnostic(Range(it.range.start.line, it.range.start.character, it.range.end.line, it.range.end.character, Position(it.range.start.line, it.range.start.character), Position(it.range.end.line, it.range.end.character)), it.message, it.severity.value - 1, it.source)
        })
      }
  }

  fun openExternal(uri: String) {
    LOG.info("openExternal uri: $uri")
    BrowserUtil.browse(uri)
  }

  fun clipboardWriteText(text: String) {
    LOG.info("clipboardWriteText text: $text")
    CopyPasteManager.copyTextToClipboard(text)
  }

  fun clipboardReadText(): Thenable/*<string>*/ {
    LOG.info("clipboardReadText")
    return ThenableBuilder.createCompleted(nodeRuntime, nodeRuntime.createV8ValueString(ClipboardUtil.getTextInClipboard()))
  }

  fun showInformationMessage(message: String) {
    LOG.info("showInformationMessage: $message")
    NotificationsManager.getNotificationsManager().showNotification(NotificationGroupManager.getInstance().getNotificationGroup("Cline").createNotification("Cline", message, NotificationType.INFORMATION), project)
  }

  fun showWarningMessage(message: String) {
    showWarningMessage(message, emptyList())
  }

  fun showWarningMessage(message: String, items: List<String>) {
    LOG.info("showWarningMessage: $message")
    NotificationsManager.getNotificationsManager().showNotification(NotificationGroupManager.getInstance().getNotificationGroup("Cline").createNotification("Cline", "$message\n${items.joinToString("\n")}", NotificationType.WARNING), project)
  }

  fun showErrorMessage(message: String) {
    showErrorMessage(message, emptyList())
  }

  fun showErrorMessage(message: String, items: List<String>) {
    LOG.info("showErrorMessage: $message")
    NotificationsManager.getNotificationsManager().showNotification(NotificationGroupManager.getInstance().getNotificationGroup("Cline").createNotification("Cline", "$message\n${items.joinToString("\n")}", NotificationType.ERROR), project)
  }

  fun showOpenDialog(options: Map<String, Any>): Thenable/*<vscode.Uri[]>*/ {
    LOG.info("showOpenDialog: $options")
    val canSelectMany = options["canSelectMany"] as Boolean
    val openLabel = options["openLabel"] as String
    val filters = options["filters"] as Map<*, *>
    val descriptor = if (canSelectMany) {
      FileChooserDescriptorFactory.multiFiles()
    }
    else {
      FileChooserDescriptorFactory.singleFile()
    }
    descriptor.withTitle(openLabel)
    for (filter in filters) {
      val name = filter.key as String
      val extensionsList = filter.value as List<*>
      val extensions = extensionsList.map { it as String }.toTypedArray()
      descriptor.withExtensionFilter(name, *extensions)
    }
    val result = CompletableFuture<List<Uri>>()
    scope.launch {
      withContext(Dispatchers.EDT) {
        var files = FileChooser.chooseFiles(descriptor, project, null)
        result.complete(files.map { Uri(it.path) }.toList())
      }
    }
    return ThenableBuilder.create(nodeRuntime, result)
  }

  fun showSaveDialog(options: Map<String, Any>): Thenable/*<vscode.Uri>*/ {
    LOG.info("showSaveDialog: $options")
    val defaultUri = (options["defaultUri"] as Map<*, *>)["filePath"] as String
    val filters = options["filters"] as Map<*, *>
    val descriptor = FileSaverDescriptorFactory.createSingleFileNoJarsDescriptor().withTitle("Save File")
    for (filter in filters) {
      val name = filter.key as String
      val extensionsList = filter.value as List<*>
      val extensions = extensionsList.map { it as String }.toTypedArray()
      descriptor.withExtensionFilter(name, *extensions)
    }
    val result = CompletableFuture<Uri?>()
    scope.launch {
      withContext(Dispatchers.EDT) {
        var file = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project).save(defaultUri)
        result.complete(file?.let { Uri(it.file.path) })
      }
    }
    return ThenableBuilder.create(nodeRuntime, result)
  }

  override fun dispose() {
  }
}