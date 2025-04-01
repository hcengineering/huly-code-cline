import { regexSearchFiles } from "../../cline/src/services/ripgrep";
import * as vscode from "vscode";
import path from "path";
import os from "os";

export async function testRipgrep() {
  console.log("test ripgrep");
  const cwd = vscode.workspace.workspaceFolders?.map((folder) => folder.uri.fsPath).at(0) ?? path.join(os.homedir(), "Desktop")
  const directoryPath = "src"
  const regex = "^import"
  var res = await regexSearchFiles(cwd, directoryPath, regex);
  console.log("!!! result", res);
}