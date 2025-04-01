import path from "path";
import os from "os"
import * as fs from "fs/promises"
import * as vscode from "vscode"
import { DiffViewProvider } from "../../cline/src/integrations/editor/DiffViewProvider";
import { randomInt } from "crypto";

export async function testDiff() {
  const cwd = vscode.workspace.workspaceFolders?.map((folder) => folder.uri.fsPath).at(0) ?? path.join(os.homedir(), "Desktop")
  const absolutePath = path.resolve(cwd, "README2.md")

  var originalContent = await fs.readFile(absolutePath, "utf-8")
  var idx = 0

  var diffProvider = new DiffViewProvider(cwd);
  diffProvider.editType = "modify";
  console.log("!!!! start1");

  await diffProvider.open("README.md")

  var isFinal = false
  while (!isFinal) {
    var offset = 50 + randomInt(50)
    if (idx + offset >= originalContent.length) {
      offset = originalContent.length - idx
      isFinal = true
    }
    await new Promise(f => setTimeout(f, 1000));
    var newContent = originalContent.substring(0, idx + offset)
    diffProvider.update(newContent, isFinal)
    idx += offset
  }
  var result = await diffProvider.saveChanges();
  console.log("!!!! start6", result);
}
