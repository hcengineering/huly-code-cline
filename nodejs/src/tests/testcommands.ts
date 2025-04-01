import * as vscode from "vscode"

export async function testCommands() {
    await new Promise(f => setTimeout(f, 10000));
  await vscode.commands.executeCommand("workbench.action.terminal.copySelection")
}