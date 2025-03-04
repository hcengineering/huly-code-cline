// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import { Cline } from "../cline/src/core/Cline"
import { ClineProvider } from "../cline/src/core/webview/ClineProvider"

import * as vscode from "vscode"

interface Thenable<T> extends PromiseLike<T> { }

console.log("Loading extension")
declare var webview: any

var sidebarProvider: ClineProvider

export function activate() {
  var context = new vscode.ExtensionContext()
  var outputChannel = {
    name: "Cline",
    appendLine: (value: string) => context.log(value + '\n'),
    append: (value: string) => context.log(value),
    show: () => { },
    hide: () => { },
    replace: () => { },
    clear: () => { },
    dispose: () => { },
  }

  sidebarProvider = new ClineProvider(context, outputChannel)
  sidebarProvider.resolveWebviewView(webview)
}

export function invokeCallback(callback: any, str: string) {
  const obj = JSON.parse(str);
  console.log("!!invoke ", callback, str);
  callback(obj);
}

export async function newChat() {
  await sidebarProvider.clearTask()
  await sidebarProvider.postStateToWebview()
  await sidebarProvider.postMessageToWebview({
    type: "action",
    action: "chatButtonClicked",
  })
}
