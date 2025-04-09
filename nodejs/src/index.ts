// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import { WebviewProvider } from "../cline/src/core/webview";
import process from 'process';
import * as vscode from "vscode"
import "../cline/src/utils/path" // necessary to have access to String.prototype.toPosix
//import { testDiff } from "./tests/difftest";

interface Thenable<T> extends PromiseLike<T> { }

console.log("Loading extension")
declare var webview: any

var sidebarProvider: WebviewProvider

process.on('unhandledRejection', (reason, promise) => {
  console.log("!!!!! ", reason, promise);
});

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
  try {
    //await testDiff();
    sidebarProvider = new WebviewProvider(context, outputChannel)
    sidebarProvider.resolveWebviewView(webview)
  } catch (e) {
    console.log(e)
  }
}

export function invokeCallback(callback: any, str: string) {
  const obj = JSON.parse(str);
  console.log("!!invoke ", str);
  callback(obj);
}

export async function newChat() {
  await sidebarProvider.controller.clearTask()
  await sidebarProvider.controller.postStateToWebview()
  await sidebarProvider.controller.postMessageToWebview({
    type: "action",
    action: "chatButtonClicked",
  })
}

export async function handleAuthCallback(state: string, token: string, apiKey: string) {
  if (!(await sidebarProvider.controller.validateAuthState(state))) {
    vscode.window.showErrorMessage("Invalid auth state")
    return
  }
  await sidebarProvider.controller.handleAuthCallback(token, apiKey)
}
