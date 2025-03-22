import * as vscode from "vscode";

export interface IHulyCode {
  getSecret(key: string): string | undefined;
  storeSecret(key: string, value: string): void;
  deleteSecret(key: string): void;
  getGlobalStoragePath: () => string;
  log(msg: string): void;

  getConfiguration(section: string | undefined, key: string): vscode.Thenable<string | undefined>;
  updateConfiguration(section: string | undefined, key: string, value: string): vscode.Thenable<void>;
  hasConfiguration(section: string | undefined, key: string): boolean;

  globalStateKeys(): string[];
  globalStateGet(key: string): string | undefined;
  globalStateUpdate(key: string, value: string | undefined): void;

  getExtension<T = any>(extensionId: string): vscode.Extension<T> | undefined;

  workspaceFolders(): string[];

  getTabs(): vscode.Tab[];
  getActiveTextEditor(): string | undefined;
  getVisibleTextEditors(): string[];
  setEditorDecorations(fsPath: string, key: string, rangesOrOptions: readonly Range[]): void;

  createTerminal(options: any): vscode.Terminal;
}
