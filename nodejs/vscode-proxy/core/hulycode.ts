import * as vscode from "vscode";

export interface IHulyCode {
  getPluginVersion(): string;
  getSecret(key: string): string | undefined;
  storeSecret(key: string, value: string): void;
  deleteSecret(key: string): void;
  getGlobalStoragePath: () => string;
  log(msg: string): void;

  getConfiguration(section: string | undefined, key: string): vscode.Thenable<string | undefined>;
  updateConfiguration(section: string | undefined, key: string, value: string, force?: boolean): vscode.Thenable<void>;
  hasConfiguration(section: string | undefined, key: string): boolean;

  globalStateKeys(): string[];
  globalStateGet(key: string): string | undefined;
  globalStateUpdate(key: string, value: string | undefined): void;

  getExtension<T = any>(extensionId: string): vscode.Extension<T> | undefined;

  workspaceFolders(): string[];

  getTabs(): any[];
  closeTab(fsPath: string): void;
  getActiveTextEditor(): vscode.TextEditor | undefined;
  getVisibleTextEditors(): vscode.TextEditor[];
  onDidChangeActiveTextEditor: vscode.Event<vscode.TextEditor | undefined>;

  createTerminal(options: any): vscode.Terminal;

  executeCommand(command: string, ...rest: any[]): vscode.Thenable<void>;
  applyWorkspaceEdit(edit: vscode.WorkspaceEdit): vscode.Thenable<boolean>;

  openTextDocument(uri: vscode.Uri): vscode.Thenable<vscode.TextDocument>;
  showTextDocument(uri: vscode.Uri | vscode.TextDocument, options?: vscode.TextDocumentShowOptions): vscode.Thenable<vscode.TextEditor>;

  getDiagnostics(): { file: string, diagnostics: vscode.Diagnostic[] }[];
  openExternal(target: string): void;

  clipboardWriteText(text: string): void;
  clipboardReadText(): vscode.Thenable<string>;

  showInformationMessage(message: string): void;
  showWarningMessage(message: string, items: string[]): void;
  showErrorMessage(message: string, items: string[]): void;

  showOpenDialog(options?: vscode.OpenDialogOptions): vscode.Thenable<vscode.Uri[] | undefined>;
  showSaveDialog(options?: vscode.SaveDialogOptions): vscode.Thenable<vscode.Uri | undefined>;
}
