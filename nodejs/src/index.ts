// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import { Cline } from "../cline/src/core/Cline"
import { ClineProvider } from "../cline/src/core/webview/ClineProvider"
import process from 'process';
import * as vscode from "vscode"
import "../cline/src/utils/path" // necessary to have access to String.prototype.toPosix
import { testDiff } from "./tests/difftest";

interface Thenable<T> extends PromiseLike<T> { }

console.log("Loading extension")
declare var webview: any

var sidebarProvider: ClineProvider

process.on('unhandledRejection', (reason, promise) => {
  console.log("!!!!! ", reason, promise);
});

export async function activate() {
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
    sidebarProvider = new ClineProvider(context, outputChannel)
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
  await sidebarProvider.clearTask()
  await sidebarProvider.postStateToWebview()
  await sidebarProvider.postMessageToWebview({
    type: "action",
    action: "chatButtonClicked",
  })
}
