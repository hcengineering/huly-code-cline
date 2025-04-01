import path from "path";
import os from "os"
import * as vscode from "vscode"
import "../../cline/src/utils/path" // necessary to have access to String.prototype.toPosix
import { diagnosticsToProblemsString } from "../../cline/src/integrations/diagnostics";

export async function testDiagnostic() {
  const cwd = vscode.workspace.workspaceFolders?.map((folder) => folder.uri.fsPath).at(0) ?? path.join(os.homedir(), "Desktop")
  await new Promise(f => setTimeout(f, 10000));

  const diagnostics = vscode.languages.getDiagnostics()
  const result = diagnosticsToProblemsString(
    diagnostics,
    [vscode.DiagnosticSeverity.Error, vscode.DiagnosticSeverity.Warning],
    cwd,
  )
  console.log(result);
}