import { Uri } from "./core/uri";
import { Range } from "./core/range";
import { Position } from "./core/position";
import { Selection } from "./core/selection";
import { IHulyCode } from "./core/hulycode";
import * as fs from "fs";

export * from './core/range';
export * from './core/selection';
export * from './core/position';
export * from './core/uri';

export const version = '1.0.0';

// this should be override by HulyCode
declare var hulyCode: IHulyCode;

export interface Thenable<T> extends PromiseLike<T> { }

export interface CancellationToken {
  isCancellationRequested: boolean;
  onCancellationRequested: Event<any>;
}

class MutableToken implements CancellationToken {

  private _isCancelled: boolean = false;
  private _emitter: EventEmitter<any> | null = null;

  public cancel() {
    if (!this._isCancelled) {
      this._isCancelled = true;
      if (this._emitter) {
        this._emitter.fire(undefined);
        this.dispose();
      }
    }
  }

  get isCancellationRequested(): boolean {
    return this._isCancelled;
  }

  get onCancellationRequested(): Event<any> {
    if (!this._emitter) {
      this._emitter = new EventEmitter<any>();
    }
    return this._emitter.event;
  }

  public dispose(): void {
    if (this._emitter) {
      this._emitter.dispose();
      this._emitter = null;
    }
  }
}

export class CancellationTokenSource {

  private _token?: CancellationToken = undefined;

  constructor(parent?: CancellationToken) {
  }

  get token(): CancellationToken {
    console.log('Get token');
    if (!this._token) {
      this._token = new MutableToken();
    }
    return this._token;
  }

  cancel(): void {
    console.log('Cancel called');
  }



  dispose(cancel: boolean = false): void {
    console.log('Dispose called');
  }
}

export class CancellationError extends Error {
  constructor() {
    super();
  }
}

export enum ViewColumn {
  Active = -1,
  Beside = -2,
  One = 1,
  Two = 2,
  Three = 3,
  Four = 4,
  Five = 5,
  Six = 6,
  Seven = 7,
  Eight = 8,
  Nine = 9
}

export class Clipboard {
  writeText(value: string): Thenable<void> {
    hulyCode.clipboardWriteText(value)
    return Promise.resolve()
  }
  readText(): Thenable<string> {
    return hulyCode.clipboardReadText()
  }
}

export namespace env {
  // export const appName: string = "Huly Code";

  export const appRoot: string = hulyCode.getGlobalStoragePath();
  // export const appHost: string = "desktop";

  export const uriScheme: string = "";
  // export const language: string;
  // export const clipboard: Clipboard;
  export const machineId: string = "";
  // export const sessionId: string;
  // export const isNewAppInstall: boolean;
  // export const isTelemetryEnabled: boolean;
  // export const onDidChangeTelemetryEnabled: Event<boolean>;
  // export const onDidChangeShell: Event<string>;
  // export function createTelemetryLogger(sender: TelemetrySender, options?: TelemetryLoggerOptions): TelemetryLogger;
  // export const remoteName: string | undefined;
  // export const shell: string;
  // export const uiKind: UIKind;
  export function openExternal(target: Uri): Thenable<boolean> {
    hulyCode.openExternal(target.fsPath)
    return Promise.resolve(true)
  }
  // export function asExternalUri(target: Uri): Thenable<Uri>;
  // export const logLevel: LogLevel;
  // export const onDidChangeLogLevel: Event<LogLevel>;
  export const clipboard: Clipboard = new Clipboard();
}

export interface TabInputOriginal {
  readonly scheme: string;
}

export class TabInputText {
  readonly uri: Uri;
  readonly modified: Uri;

  constructor(uri: Uri) {
    this.uri = uri;
    this.modified = uri;
  }
}

export class TabInputTextDiff {
  readonly uri: Uri;
  readonly modified: Uri;
  readonly original: TabInputOriginal;

  constructor(uri: Uri) {
    this.uri = uri;
    this.modified = uri;
    this.original = {
      scheme: "cline-diff"
    }
  }
}

export interface Tab {

  readonly label: string;

  readonly group: TabGroup;

  readonly input: TabInputText | unknown;

  readonly isActive: boolean;

  readonly isDirty: boolean;

  readonly isPinned: boolean;

  readonly isPreview: boolean;
}


export interface TabGroup {
  readonly isActive: boolean;
  viewColumn: ViewColumn;

  // 		readonly activeTab: Tab | undefined;

  readonly tabs: readonly Tab[];
}

export interface TabGroups {
  readonly all: readonly TabGroup[];
  //   readonly activeTabGroup: TabGroup;
  //   readonly onDidChangeTabGroups: Event<TabGroupChangeEvent>;
  //   readonly onDidChangeTabs: Event<TabChangeEvent>;
  close(tab: Tab | readonly Tab[], preserveFocus?: boolean): Thenable<boolean>;
  close(tabGroup: TabGroup | readonly TabGroup[], preserveFocus?: boolean): Thenable<boolean>;
}

export class ThemeColor {
  constructor(id: string) {
    console.log("ThemeColor(" + id + ")");
  }
}

export interface ThemableDecorationRenderOptions {
  backgroundColor?: string | ThemeColor;
  //   outline?: string;
  //   outlineColor?: string | ThemeColor;
  //   outlineStyle?: string;
  //   outlineWidth?: string;
  border?: string;
  //   borderColor?: string | ThemeColor;
  //   borderRadius?: string;
  //   borderSpacing?: string;
  //   borderStyle?: string;
  //   borderWidth?: string;
  //   fontStyle?: string;
  //   fontWeight?: string;
  //   textDecoration?: string;
  //   cursor?: string;
  //   color?: string | ThemeColor;
  opacity?: string;
  //   letterSpacing?: string;
  //   gutterIconPath?: string | Uri;
  //   gutterIconSize?: string;
  //   overviewRulerColor?: string | ThemeColor;
  //   before?: ThemableDecorationAttachmentRenderOptions;
  //   after?: ThemableDecorationAttachmentRenderOptions;
}


export interface DecorationRenderOptions extends ThemableDecorationRenderOptions {
  isWholeLine?: boolean;
  //   rangeBehavior?: DecorationRangeBehavior;
  //   overviewRulerLane?: OverviewRulerLane;
  //   light?: ThemableDecorationRenderOptions;
  //   dark?: ThemableDecorationRenderOptions;
}

export interface TextEditorOptions {
  tabSize?: number | string;
  indentSize?: number | string;
  insertSpaces?: boolean | string;
  //cursorStyle?: TextEditorCursorStyle;
  //lineNumbers?: TextEditorLineNumbersStyle;
}

export enum TextEditorRevealType {
  Default = 0,
  InCenter = 1,
  InCenterIfOutsideViewport = 2,
  AtTop = 3
}

export interface TextEditorDecorationType {
  readonly key: string;
  dispose(): void;
}

export interface SaveDialogOptions {
  defaultUri?: Uri;

  saveLabel?: string;

  filters?: { [name: string]: string[] };

  title?: string;
}

export interface TextDocumentShowOptions {
  viewColumn?: ViewColumn;
  preserveFocus?: boolean;
  preview?: boolean;
  selection?: Range;
}

export enum TerminalExitReason {
  Unknown = 0,
  Shutdown = 1,
  Process = 2,
  User = 3,
  Extension = 4,
}

export interface TerminalExitStatus {
  readonly code: number | undefined;
  readonly reason: TerminalExitReason;
}

export interface Terminal {
  //   readonly name: string;
  //readonly processId: Thenable<number | undefined>;
  //   readonly creationOptions: Readonly<TerminalOptions | ExtensionTerminalOptions>;
  readonly exitStatus: TerminalExitStatus | undefined;
  //   readonly state: TerminalState;
  sendText(text: string, addNewLine?: boolean): void;
  show(preserveFocus?: boolean): void;
  hide(): void;
  dispose(): void;
}

export interface TerminalOptions {
  name?: string;
  //   shellPath?: string;

  shellArgs?: string[] | string;
  cwd?: string | Uri;
  env?: { [key: string]: string | null | undefined };
  strictEnv?: boolean;
  hideFromUser?: boolean;
  message?: string;
  iconPath?: Uri | ThemeIcon;

  //   color?: ThemeColor;
  //   location?: TerminalLocation | TerminalEditorLocationOptions | TerminalSplitLocationOptions;
  //   isTransient?: boolean;
}

export class ThemeIcon {
  static readonly File: ThemeIcon;
  static readonly Folder: ThemeIcon;
  readonly id: string;
  readonly color?: ThemeColor | undefined;
  constructor(id: string, color?: ThemeColor) {
    this.id = id;
    this.color = color;
  }
}

export interface Window {
  onDidStartTerminalShellExecution?: (
    listener: (e: any) => any,
    thisArgs?: any,
    disposables?: Disposable[],
  ) => Disposable
}

export interface OpenDialogOptions {
  defaultUri?: Uri;
  openLabel?: string;
  canSelectFiles?: boolean;
  canSelectFolders?: boolean;
  canSelectMany?: boolean;
  filters?: { [name: string]: string[] };
  title?: string;
}

export namespace window {

  export const tabGroups: TabGroups = {
    get all(): TabGroup[] {
      return [{
        isActive: true,
        viewColumn: ViewColumn.One,
        tabs: hulyCode.getTabs().map((tab) => {
          var input = tab.isDiff() ? new TabInputTextDiff(Uri.file(tab.path)) : new TabInputText(Uri.file(tab.path));
          return {
            label: tab.path,
            input: input,
            isActive: false,
            isDirty: tab.isDirty(),
            isPinned: false,
            isPreview: false,
          } as Tab
        }),
      }]
    },
    close: function(tabGroup: TabGroup | Tab | readonly Tab[] | readonly TabGroup[], preserveFocus?: boolean): Thenable<boolean> {
      var tab = tabGroup as any;
      if (tab.input) {
        hulyCode.closeTab(tab.input.uri.fsPath);
      }
      return Promise.resolve(true);
    }
  };

  export let activeTextEditor: TextEditor | undefined = hulyCode.getActiveTextEditor();

  export let visibleTextEditors: readonly TextEditor[] = hulyCode.getVisibleTextEditors();

  export const onDidChangeActiveTextEditor: Event<TextEditor | undefined> = hulyCode.onDidChangeActiveTextEditor;

  // export const onDidChangeVisibleTextEditors: Event<readonly TextEditor[]>;

  // export const onDidChangeTextEditorSelection: Event<TextEditorSelectionChangeEvent>;

  // export const onDidChangeTextEditorVisibleRanges: Event<TextEditorVisibleRangesChangeEvent>;

  // export const onDidChangeTextEditorOptions: Event<TextEditorOptionsChangeEvent>;

  // export const onDidChangeTextEditorViewColumn: Event<TextEditorViewColumnChangeEvent>;

  // export const visibleNotebookEditors: readonly NotebookEditor[];

  // export const onDidChangeVisibleNotebookEditors: Event<readonly NotebookEditor[]>;

  // export const activeNotebookEditor: NotebookEditor | undefined;

  // export const onDidChangeActiveNotebookEditor: Event<NotebookEditor | undefined>;

  // export const onDidChangeNotebookEditorSelection: Event<NotebookEditorSelectionChangeEvent>;

  // export const onDidChangeNotebookEditorVisibleRanges: Event<NotebookEditorVisibleRangesChangeEvent>;

  // export const terminals: readonly Terminal[];

  // export const activeTerminal: Terminal | undefined;

  // export const onDidChangeActiveTerminal: Event<Terminal | undefined>;

  // export const onDidOpenTerminal: Event<Terminal>;

  // export const onDidCloseTerminal: Event<Terminal>;

  // export const onDidChangeTerminalState: Event<Terminal>;

  // export const state: WindowState;

  // export const onDidChangeWindowState: Event<WindowState>;

  //   export function showTextDocument(document: TextDocument, column?: ViewColumn, preserveFocus?: boolean): Thenable<TextEditor> {
  //     console.log(`showTextDocument: ${document}`);
  //     return Promise.resolve<any>(undefined);
  //   }

  //   export function showTextDocument(document: TextDocument, options?: TextDocumentShowOptions): Thenable<TextEditor> {
  //     console.log(`showTextDocument: ${document}`);
  //     return Promise.resolve<any>(undefined);
  //   }

  export function showTextDocument(uri: Uri | TextDocument, options?: TextDocumentShowOptions): Thenable<TextEditor> {
    return hulyCode.showTextDocument(uri, options);
  }

  // export function showNotebookDocument(document: NotebookDocument, options?: NotebookDocumentShowOptions): Thenable<NotebookEditor>;

  export function createTextEditorDecorationType(options: DecorationRenderOptions): TextEditorDecorationType {
    return {
      key: options.opacity === "1" ? "active" : "faded",
      dispose: () => {
        console.log("createTextEditorDecorationType.dispose");
      }
    };
  }

  export function showInformationMessage<T extends string>(message: string, ...items: T[]): Thenable<T | undefined> {
    hulyCode.showInformationMessage(message);
    return Promise.resolve<any>(undefined);
  }

  export function showWarningMessage<T extends string>(message: string, ...items: T[]): Thenable<T | undefined> {
    hulyCode.showWarningMessage(message, items);
    return Promise.resolve<any>(undefined);
  }

  export function showErrorMessage<T extends string>(message: string, ...items: T[]): Thenable<T | undefined> {
    hulyCode.showErrorMessage(message, items);
    return Promise.resolve<any>(undefined);
  }

  export function showOpenDialog(options?: OpenDialogOptions): Thenable<Uri[] | undefined> {
    return hulyCode.showOpenDialog(options);
  }

  export function showSaveDialog(options?: SaveDialogOptions): Thenable<Uri | undefined> {
    return hulyCode.showSaveDialog(options);
  }

  // export function showInputBox(options?: InputBoxOptions, token?: CancellationToken): Thenable<string | undefined>;

  // export function createQuickPick<T extends QuickPickItem>(): QuickPick<T>;

  // export function createInputBox(): InputBox;

  // export function createOutputChannel(name: string, languageId?: string): OutputChannel;

  // export function createOutputChannel(name: string, options: { /** literal-type defines return type */log: true }): LogOutputChannel;

  // export function createWebviewPanel(viewType: string, title: string, showOptions: ViewColumn | {
  //     readonly viewColumn: ViewColumn;
  //     readonly preserveFocus?: boolean;
  // }, options?: WebviewPanelOptions & WebviewOptions): WebviewPanel;

  // export function setStatusBarMessage(text: string, hideAfterTimeout: number): Disposable;

  // export function setStatusBarMessage(text: string, hideWhenDone: Thenable<any>): Disposable;

  // export function setStatusBarMessage(text: string): Disposable;

  // export function withScmProgress<R>(task: (progress: Progress<number>) => Thenable<R>): Thenable<R>;

  // export function withProgress<R>(options: ProgressOptions, task: (progress: Progress<{
  //     message?: string;
  //     increment?: number;
  // }>, token: CancellationToken) => Thenable<R>): Thenable<R>;

  // export function createStatusBarItem(id: string, alignment?: StatusBarAlignment, priority?: number): StatusBarItem;

  // export function createStatusBarItem(alignment?: StatusBarAlignment, priority?: number): StatusBarItem;

  //   export function createTerminal(name?: string, shellPath?: string, shellArgs?: readonly string[] | string): Terminal {
  //   }

  export function createTerminal(options: TerminalOptions): Terminal {
    return hulyCode.createTerminal(options);
  }

  // export function createTerminal(options: ExtensionTerminalOptions): Terminal;

  // export function registerTreeDataProvider<T>(viewId: string, treeDataProvider: TreeDataProvider<T>): Disposable;

  // export function createTreeView<T>(viewId: string, options: TreeViewOptions<T>): TreeView<T>;

  // export function registerUriHandler(handler: UriHandler): Disposable;

  // export function registerWebviewPanelSerializer(viewType: string, serializer: WebviewPanelSerializer): Disposable;

  // export function registerWebviewViewProvider(viewId: string, provider: WebviewViewProvider, options?: {
  //     readonly webviewOptions?: {
  //         readonly retainContextWhenHidden?: boolean;
  //     };
  // }): Disposable;

  // export function registerCustomEditorProvider(viewType: string, provider: CustomTextEditorProvider | CustomReadonlyEditorProvider | CustomEditorProvider, options?: {
  //     readonly webviewOptions?: WebviewPanelOptions;

  //     readonly supportsMultipleEditorsPerDocument?: boolean;
  // }): Disposable;

  // export function registerTerminalLinkProvider(provider: TerminalLinkProvider): Disposable;

  // export function registerTerminalProfileProvider(id: string, provider: TerminalProfileProvider): Disposable;
  // export function registerFileDecorationProvider(provider: FileDecorationProvider): Disposable;

  // export let activeColorTheme: ColorTheme;

  // export const onDidChangeActiveColorTheme: Event<ColorTheme>;
}

export class Disposable {
  static from(...disposableLikes: {
    dispose: () => any;
  }[]): Disposable {
    console.log("Disposable.from called");
    return new Disposable(() => {
      disposableLikes.forEach((d) => d.dispose());
    });
  }

  constructor(callOnDispose: () => any) {
    console.log("Disposable created");
  }

  dispose(): any {
    console.log("Disposable.dispose called");
  }
}

export interface Event<T> {
  (listener: (e: T) => any, thisArgs?: any, disposables?: Disposable[]): Disposable;
}

export interface FileCreateEvent {
  readonly files: readonly Uri[];
}

export interface FileDeleteEvent {
  readonly files: readonly Uri[];
}


export interface FileRenameEvent {
  readonly files: ReadonlyArray<{
    readonly oldUri: Uri;
    readonly newUri: Uri;
  }>;
}

export enum FileType {
  Unknown = 0,
  File = 1,
  Directory = 2,
  SymbolicLink = 64
}

export enum FilePermission {
  Readonly = 1
}

export interface FileStat {
  type: FileType;
  ctime: number;
  mtime: number;
  size: number;
  permissions?: FilePermission;
}

export class EventEmitter<T> {
  private _disposed?: true;
  private _event?: Event<T>;

  // protected _listeners?: ListenerOrListeners<T>;

  //  _deliveryQueue?: EventDeliveryQueuePrivate;
  protected _size = 0;

  constructor() {
  }

  dispose() {
    if (!this._disposed) {
      this._disposed = true;

      // if (this._listeners) {
      // 	if (_enableDisposeWithListenerWarning) {
      // 		const listeners = this._listeners;
      // 		queueMicrotask(() => {
      // 			forEachListener(listeners, l => l.stack?.print());
      // 		});
      // 	}

      // 	this._listeners = undefined;
      // 	this._size = 0;
      // }
      // this._options?.onDidRemoveLastListener?.();
      // this._leakageMon?.dispose();
    }
  }

  get event(): Event<T> {
    this._event ??= (callback: (e: T) => unknown, thisArgs?: any, disposables?: Disposable[] | Disposable) => {
      // if (this._leakageMon && this._size > this._leakageMon.threshold ** 2) {
      // 	const message = `[${this._leakageMon.name}] REFUSES to accept new listeners because it exceeded its threshold by far (${this._size} vs ${this._leakageMon.threshold})`;
      // 	console.warn(message);

      // 	const tuple = this._leakageMon.getMostFrequentStack() ?? ['UNKNOWN stack', -1];
      // 	const error = new ListenerRefusalError(`${message}. HINT: Stack shows most frequent listener (${tuple[1]}-times)`, tuple[0]);
      // 	const errorHandler = this._options?.onListenerError || onUnexpectedError;
      // 	errorHandler(error);

      // 	return Disposable.None;
      // }

      // if (this._disposed) {
      // 	// todo: should we warn if a listener is added to a disposed emitter? This happens often
      // 	return Disposable.None;
      // }

      // if (thisArgs) {
      // 	callback = callback.bind(thisArgs);
      // }

      // const contained = new UniqueContainer(callback);

      // let removeMonitor: Function | undefined;
      // let stack: Stacktrace | undefined;
      // if (this._leakageMon && this._size >= Math.ceil(this._leakageMon.threshold * 0.2)) {
      // 	// check and record this emitter for potential leakage
      // 	contained.stack = Stacktrace.create();
      // 	removeMonitor = this._leakageMon.check(contained.stack, this._size + 1);
      // }

      // if (_enableDisposeWithListenerWarning) {
      // 	contained.stack = stack ?? Stacktrace.create();
      // }

      // if (!this._listeners) {
      // 	this._options?.onWillAddFirstListener?.(this);
      // 	this._listeners = contained;
      // 	this._options?.onDidAddFirstListener?.(this);
      // } else if (this._listeners instanceof UniqueContainer) {
      // 	this._deliveryQueue ??= new EventDeliveryQueuePrivate();
      // 	this._listeners = [this._listeners, contained];
      // } else {
      // 	this._listeners.push(contained);
      // }
      // this._options?.onDidAddListener?.(this);

      // this._size++;


      const result = new Disposable(() => {
        // removeMonitor?.();
        // this._removeListener(contained);
      });
      //  if (disposables instanceof DisposableStore) {
      //  	disposables.add(result);
      //  } else if (Array.isArray(disposables)) {
      //  	disposables.push(result);
      //  }

      return result;
    };

    return this._event;
  }

  // private _removeListener(listener: ListenerContainer<T>) {
  // 	this._options?.onWillRemoveListener?.(this);

  // 	if (!this._listeners) {
  // 		return; // expected if a listener gets disposed
  // 	}

  // 	if (this._size === 1) {
  // 		this._listeners = undefined;
  // 		this._options?.onDidRemoveLastListener?.(this);
  // 		this._size = 0;
  // 		return;
  // 	}

  // 	// size > 1 which requires that listeners be a list:
  // 	const listeners = this._listeners as (ListenerContainer<T> | undefined)[];

  // 	const index = listeners.indexOf(listener);
  // 	if (index === -1) {
  // 		console.log('disposed?', this._disposed);
  // 		console.log('size?', this._size);
  // 		console.log('arr?', JSON.stringify(this._listeners));
  // 		throw new Error('Attempted to dispose unknown listener');
  // 	}

  // 	this._size--;
  // 	listeners[index] = undefined;

  // 	const adjustDeliveryQueue = this._deliveryQueue!.current === this;
  // 	if (this._size * compactionThreshold <= listeners.length) {
  // 		let n = 0;
  // 		for (let i = 0; i < listeners.length; i++) {
  // 			if (listeners[i]) {
  // 				listeners[n++] = listeners[i];
  // 			} else if (adjustDeliveryQueue && n < this._deliveryQueue!.end) {
  // 				this._deliveryQueue!.end--;
  // 				if (n < this._deliveryQueue!.i) {
  // 					this._deliveryQueue!.i--;
  // 				}
  // 			}
  // 		}
  // 		listeners.length = n;
  // 	}
  // }

  // private _deliver(listener: undefined | UniqueContainer<(value: T) => void>, value: T) {
  // 	if (!listener) {
  // 		return;
  // 	}

  // 	const errorHandler = this._options?.onListenerError || onUnexpectedError;
  // 	if (!errorHandler) {
  // 		listener.value(value);
  // 		return;
  // 	}

  // 	try {
  // 		listener.value(value);
  // 	} catch (e) {
  // 		errorHandler(e);
  // 	}
  // }

  // /** Delivers items in the queue. Assumes the queue is ready to go. */
  // private _deliverQueue(dq: EventDeliveryQueuePrivate) {
  // 	const listeners = dq.current!._listeners! as (ListenerContainer<T> | undefined)[];
  // 	while (dq.i < dq.end) {
  // 		// important: dq.i is incremented before calling deliver() because it might reenter deliverQueue()
  // 		this._deliver(listeners[dq.i++], dq.value as T);
  // 	}
  // 	dq.reset();
  // }

  /**
   * To be kept private to fire an event to
   * subscribers
   */
  fire(event: T): void {
    // if (this._deliveryQueue?.current) {
    // 	this._deliverQueue(this._deliveryQueue);
    // 	this._perfMon?.stop(); // last fire() will have starting perfmon, stop it before starting the next dispatch
    // }

    // this._perfMon?.start(this._size);

    // if (!this._listeners) {
    // 	// no-op
    // } else if (this._listeners instanceof UniqueContainer) {
    // 	this._deliver(this._listeners, event);
    // } else {
    // 	const dq = this._deliveryQueue!;
    // 	dq.enqueue(this, event, this._listeners.length);
    // 	this._deliverQueue(dq);
    // }

    // this._perfMon?.stop();
  }

  hasListeners(): boolean {
    return this._size > 0;
  }
}

export interface WorkspaceFolder {
  readonly uri: Uri;
  readonly name: string;
  readonly index: number;
}

export interface TextLine {
  readonly lineNumber: number;
  readonly text: string;
  readonly range: Range;
  readonly rangeIncludingLineBreak: Range;
  readonly firstNonWhitespaceCharacterIndex: number;
  readonly isEmptyOrWhitespace: boolean;
}

export interface TextDocument {
  readonly uri: Uri;
  //readonly fileName: string;
  //readonly isUntitled: boolean;
  readonly languageId: string;
  //readonly version: number;
  readonly isDirty: boolean;
  //readonly isClosed: boolean;
  save(): Thenable<boolean>;
  //readonly eol: EndOfLine;
  readonly lineCount: number;
  //lineAt(line: number): TextLine;
  lineAt(position: Position): TextLine;
  //offsetAt(position: Position): number;
  positionAt(offset: number): Position;
  getText(range?: Range): string;
  //getWordRangeAtPosition(position: Position, regex?: RegExp): Range | undefined;
  //validateRange(range: Range): Range;
  //validatePosition(position: Position): Position;
}

export enum ExtensionKind {
  UI = 1,
  Workspace = 2
}

export interface Extension<T> {
  readonly id: string;
  readonly extensionUri: Uri;
  readonly extensionPath: string;
  readonly isActive: boolean;
  readonly packageJSON: any;
  extensionKind: ExtensionKind;
  readonly exports: T;
  //activate(): Thenable<T>;
}

export interface FileSystemWatcher extends Disposable {
  // readonly ignoreCreateEvents: boolean;

  // readonly ignoreChangeEvents: boolean;

  // readonly ignoreDeleteEvents: boolean;

  readonly onDidCreate: Event<Uri>;
  readonly onDidChange: Event<Uri>;
  readonly onDidDelete: Event<Uri>;
}

export class RelativePattern {
  readonly baseUri: String;
  readonly pattern: string;
  constructor(baseUri: String, pattern: string) {
    this.baseUri = baseUri;
    this.pattern = pattern;
  }
}

export class HulyCodeFileSystem {
  stat(uri: Uri): Thenable<FileStat> {
    console.log('MockFileSystem.stat called with uri:', uri);
    return Promise.resolve({
      type: FileType.File,
      ctime: Date.now(),
      mtime: Date.now(),
      size: 0,
      permissions: undefined
    });
  }

  // readDirectory(uri: Uri): Thenable<[string, FileType][]>;

  // createDirectory(uri: Uri): Thenable<void>;

  // readFile(uri: Uri): Thenable<Uint8Array>;

  writeFile(uri: Uri, content: Uint8Array): Thenable<void> {
    fs.writeFileSync(uri.fsPath, content);
    return Promise.resolve<any>(undefined);
  }

//   delete(uri: Uri, options?: {
//       recursive?: boolean;
//       useTrash?: boolean;
//   }): Thenable<void>;

//   rename(source: Uri, target: Uri, options?: {
//       overwrite?: boolean;
//   }): Thenable<void>;

  // copy(source: Uri, target: Uri, options?: {
  //     overwrite?: boolean;
  // }): Thenable<void>;

//   isWritableFileSystem(scheme: string): boolean | undefined;
}

export interface WorkspaceConfiguration {
  get<T>(section: string): T | undefined;
  get<T>(section: string, defaultValue: T): T;
  has(section: string): boolean;
  update(section: string, value: any, force?: boolean): Thenable<void>;
}

export interface ConfigurationChangeEvent {
  affectsConfiguration(section: string): boolean;
}

export class WorkspaceEdit {
  uri?: Uri;
  range?: Range;
  newText?: string;

  constructor() { }

  replace(uri: Uri, range: Range, newText: string): void {
    this.uri = uri;
    this.range = range;
    this.newText = newText;
  }

  delete(uri: Uri, range: Range): void {
    this.uri = uri;
    this.range = range;
  }
}

export namespace workspace {

  export const fs: HulyCodeFileSystem = new HulyCodeFileSystem();

  // export const rootPath: string | undefined;

  export const workspaceFolders: WorkspaceFolder[] | undefined = hulyCode.workspaceFolders()?.map((it) => {
    return {
      uri: Uri.file(it),
      name: it,
      index: 0,
    }
  });


  // export const name: string | undefined;

  // export const workspaceFile: Uri | undefined;

  // export const onDidChangeWorkspaceFolders: Event<WorkspaceFoldersChangeEvent>;

  // export function getWorkspaceFolder(uri: Uri): WorkspaceFolder | undefined;

  // export function asRelativePath(pathOrUri: string | Uri, includeWorkspaceFolder?: boolean): string;

  // export function updateWorkspaceFolders(start: number, deleteCount: number | undefined | null, ...workspaceFoldersToAdd: {
  //     readonly uri: Uri;
  //     readonly name?: string;
  // }[]): boolean;

  export function createFileSystemWatcher(globPattern: RelativePattern): FileSystemWatcher {
    return {
      onDidCreate: new EventEmitter<Uri>().event,
      onDidChange: new EventEmitter<Uri>().event,
      onDidDelete: new EventEmitter<Uri>().event,
      dispose: () => { },
    }
  }

  // export function findFiles(include: GlobPattern, exclude?: GlobPattern | null, maxResults?: number, token?: CancellationToken): Thenable<Uri[]>;

  // export function saveAll(includeUntitled?: boolean): Thenable<boolean>;

  export function applyEdit(edit: WorkspaceEdit): Thenable<boolean> {
    return hulyCode.applyWorkspaceEdit(edit);
  }

  export const textDocuments: readonly TextDocument[] = hulyCode.getVisibleTextEditors().map((editor) => editor.document);

  export function openTextDocument(uri: Uri): Thenable<TextDocument> {
    return hulyCode.openTextDocument(uri);
  }

  // export function openTextDocument(fileName: string): Thenable<TextDocument>;

  // export function openTextDocument(options?: {
  //     language?: string;
  //     content?: string;
  // }): Thenable<TextDocument>;

  // export function registerTextDocumentContentProvider(scheme: string, provider: TextDocumentContentProvider): Disposable;

  // export const onDidOpenTextDocument: Event<TextDocument>;

  // export const onDidCloseTextDocument: Event<TextDocument>;

  // export const onDidChangeTextDocument: Event<TextDocumentChangeEvent>;

  // export const onWillSaveTextDocument: Event<TextDocumentWillSaveEvent>;

  export const onDidSaveTextDocument: Event<TextDocument> = new EventEmitter<TextDocument>().event;

  // export const notebookDocuments: readonly NotebookDocument[];

  // export function openNotebookDocument(uri: Uri): Thenable<NotebookDocument>;

  // export function openNotebookDocument(notebookType: string, content?: NotebookData): Thenable<NotebookDocument>;

  // export const onDidChangeNotebookDocument: Event<NotebookDocumentChangeEvent>;

  // export const onWillSaveNotebookDocument: Event<NotebookDocumentWillSaveEvent>;

  // export const onDidSaveNotebookDocument: Event<NotebookDocument>;

  // export function registerNotebookSerializer(notebookType: string, serializer: NotebookSerializer, options?: NotebookDocumentContentOptions): Disposable;

  // export const onDidOpenNotebookDocument: Event<NotebookDocument>;

  // export const onDidCloseNotebookDocument: Event<NotebookDocument>;

  // export const onWillCreateFiles: Event<FileWillCreateEvent>;

  export const onDidCreateFiles: Event<FileCreateEvent> = new EventEmitter<FileCreateEvent>().event;

  // export const onWillDeleteFiles: Event<FileWillDeleteEvent>;

  export const onDidDeleteFiles: Event<FileDeleteEvent> = new EventEmitter<FileDeleteEvent>().event;

  // export const onWillRenameFiles: Event<FileWillRenameEvent>;

  export const onDidRenameFiles: Event<FileRenameEvent> = new EventEmitter<FileRenameEvent>().event;

  export const onDidChangeConfiguration: Event<ConfigurationChangeEvent> = new EventEmitter<ConfigurationChangeEvent>().event;

  export function getConfiguration(section?: string): WorkspaceConfiguration {
    return {
      get: (key: string) => {
        return hulyCode.getConfiguration(section, key);
      },
      has: (key: string) => {
        return hulyCode.hasConfiguration(section, key);
      },
      update: (key: string, value: any, force?: boolean) => {
        hulyCode.updateConfiguration(section, key, value, force);
        return Promise.resolve();
      }
    }
  }

  // export const onDidChangeConfiguration: Event<ConfigurationChangeEvent>;

  // export function registerTaskProvider(type: string, provider: TaskProvider): Disposable;

  // export function registerFileSystemProvider(scheme: string, provider: FileSystemProvider, options?: {
  //     readonly isCaseSensitive?: boolean;
  //     readonly isReadonly?: boolean;
  // }): Disposable;

  // export const isTrusted: boolean;

  // export const onDidGrantWorkspaceTrust: Event<void>;
}

export namespace extensions {
  export function getExtension<T = any>(extensionId: string): Extension<T> | undefined {
    return hulyCode.getExtension(extensionId);
  }
  export const all: readonly Extension<any>[] = [
    {
      id: "",
      extensionUri: Uri.file(""),
      extensionPath: ".",
      isActive: true,
      packageJSON: {
        contributes: {
          themes: [
            {
              label: "OneDark-Pro",
              path: "OneDark-Pro.json"
            },
            {
              label: "OneLight",
              path: "OneLight.json"
            }
          ]
        }
      },
      extensionKind: ExtensionKind.Workspace,
      exports: undefined
    }
  ];
  // export const onDidChange: Event<void>;
}

export interface WebviewOptions {
  readonly enableScripts?: boolean;

  readonly enableForms?: boolean;

  readonly enableCommandUris?: boolean | readonly string[];

  readonly localResourceRoots?: readonly Uri[];
}

export interface Webview {
  options: WebviewOptions;

  html: string;

  readonly onDidReceiveMessage: Event<any>;

  postMessage(message: any): Thenable<boolean>;

  asWebviewUri(localResource: Uri): Uri;

  readonly cspSource: string;
}

export interface Memento {

  keys(): readonly string[];

  get<T>(key: string): T | undefined;

  update(key: string, value: any): Thenable<void>;
}
export interface SecretStorage {
  get(key: string): Thenable<string | undefined>;
  store(key: string, value: string): Thenable<void>;
  delete(key: string): Thenable<void>;
}

export enum ExtensionMode {
  Production = 1,
  Development = 2,
  Test = 3,
}

export class ExtensionContext {
  public get globalStorageUri(): Uri {
    return Uri.file(hulyCode.getGlobalStoragePath());
  }

  public get extensionUri(): Uri {
    return Uri.file("http://hulycline");
  }

  public extensionMode: ExtensionMode = ExtensionMode.Production;

  public extension: any = {
    packageJSON: {
      version: hulyCode.getPluginVersion()
    }
  };

  public log(msg: string) {
    hulyCode.log(msg);
  }

  readonly secrets: SecretStorage = {
    get: function(key: string): Thenable<string | undefined> {
      return Promise.resolve(hulyCode.getSecret(key));
    },
    store: function(key: string, value: string): Thenable<void> {
      hulyCode.storeSecret(key, value);
      return Promise.resolve();
    },

    delete: function(key: string): Thenable<void> {
      hulyCode.deleteSecret(key);
      return Promise.resolve();
    }
  };

  readonly workspaceState: Memento = {
    keys: function(): readonly string[] {
      return [];
    },
    get: function <T>(key: string): T | undefined {
      const value = hulyCode.globalStateGet('ws-' + key);
      if (value === undefined) {
        return undefined;
      }
      return JSON.parse(value);
    },
    update: function(key: string, value: any): Thenable<void> {
      if (value === undefined) {
        hulyCode.globalStateUpdate('ws' + key, undefined);
      } else {
        hulyCode.globalStateUpdate('ws' + key, JSON.stringify(value));
      }
      return Promise.resolve();
    }
  };

  readonly globalState: Memento = {
    keys: function(): readonly string[] {
      return hulyCode.globalStateKeys();
    },
    get: function <T>(key: string): T | undefined {
      const value = hulyCode.globalStateGet(key);
      if (value === undefined) {
        return undefined;
      }
      return JSON.parse(value);
    },
    update: function(key: string, value: any): Thenable<void> {
      if (value === undefined) {
        hulyCode.globalStateUpdate(key, undefined);
      } else {
        hulyCode.globalStateUpdate(key, JSON.stringify(value));
      }
      return Promise.resolve();
    }
  };
}

export class DiagnosticRelatedInformation {
  constructor(public location: Location, public message: string) {
  }
}

export enum DiagnosticTag {
  Unnecessary = 1,
  Deprecated = 2,
}

export enum DiagnosticSeverity {
  Error = 0,
  Warning = 1,
  Information = 2,
  Hint = 3
}

export class Diagnostic {
  source?: string;
  code?: string | number | {
    value: string | number;
    target: Uri;
  };

  relatedInformation?: DiagnosticRelatedInformation[];
  tags?: DiagnosticTag[];
  constructor(public range: Range, public message: string, public severity: DiagnosticSeverity) {
  }
}

export interface TextEditor {

  readonly document: TextDocument;

  // only set from DiffEditor
  selection: Selection;

  //selections: readonly Selection[];

  //readonly visibleRanges: readonly Range[];

  options: TextEditorOptions;

  readonly viewColumn: ViewColumn | undefined;

  // edit(callback: (editBuilder: TextEditorEdit) => void, options?: {
  //     readonly undoStopBefore: boolean;
  //     readonly undoStopAfter: boolean;
  // }): Thenable<boolean>;

  // insertSnippet(snippet: SnippetString, location?: Position | Range | readonly Position[] | readonly Range[], options?: {
  //     readonly undoStopBefore: boolean;
  //     readonly undoStopAfter: boolean;
  // }): Thenable<boolean>;

  setDecorations(decorationType: TextEditorDecorationType, rangesOrOptions: readonly Range[]): void;

  revealRange(range: Range, revealType?: TextEditorRevealType): void;

  //show(column?: ViewColumn): void;

  //hide(): void;
}

export interface OutputChannel {

  readonly name: string;

  append(value: string): void;

  appendLine(value: string): void;

  replace(value: string): void;

  clear(): void;

  show(preserveFocus?: boolean): void;

  show(column?: ViewColumn, preserveFocus?: boolean): void;

  hide(): void;

  dispose(): void;
}

export namespace languages {
  export function getDiagnostics(): [Uri, Diagnostic[]][] {
    return hulyCode.getDiagnostics().map((it) => {
      return [Uri.file(it.file), it.diagnostics];
    });
  }
}

export interface WebviewView {
  //   readonly viewType: string;
  readonly webview: Webview;
  // 		title?: string;
  // 		description?: string;
  // 		badge?: ViewBadge | undefined;
  //
  readonly onDidDispose: Event<void>;
  readonly visible: boolean;
  readonly onDidChangeVisibility: Event<void>;
  // 		show(preserveFocus?: boolean): void;
}

interface WebviewViewResolveContext<T = unknown> {
  readonly state: T | undefined;
}

export interface WebviewPanelOptions {
  readonly enableFindWidget?: boolean;

  readonly retainContextWhenHidden?: boolean;
}

export interface WebviewPanelOnDidChangeViewStateEvent {
  readonly webviewPanel: WebviewPanel;
}

export interface WebviewPanel {
  //   readonly viewType: string;
  //   title: string;
  //   iconPath?: Uri | {
  //     readonly light: Uri;
  //     readonly dark: Uri;
  //   };
  readonly webview: Webview;
  readonly options: WebviewPanelOptions;
  //   readonly viewColumn: ViewColumn | undefined;
  //   readonly active: boolean;
  readonly visible: boolean;
  readonly onDidChangeViewState: Event<WebviewPanelOnDidChangeViewStateEvent>;
  readonly onDidDispose: Event<void>;
  //   reveal(viewColumn?: ViewColumn, preserveFocus?: boolean): void;
  dispose(): any;
}

export interface WebviewViewProvider {
  resolveWebviewView(webviewView: WebviewView, context: WebviewViewResolveContext, token: CancellationToken): Thenable<void> | void;
}

export namespace commands {
  export function registerCommand(command: string, callback: (...args: any[]) => any): Disposable {
    console.log(`Command registered: ${command}`);
    return new Disposable(() => {
      console.log(`Command unregistered: ${command}`);
    });
  }

  export function executeCommand(command: string, ...rest: any[]): Thenable<void> {
    return hulyCode.executeCommand(command, rest);
  }
}

export interface DebugSession {

  readonly id: string;
  readonly type: string;
  readonly parentSession?: DebugSession;

  name: string;

  //   readonly workspaceFolder: WorkspaceFolder | undefined;
  //
  //   readonly configuration: DebugConfiguration;
  //
  //   customRequest(command: string, args?: any): Thenable<any>;
  //
  //   getDebugProtocolBreakpoint(breakpoint: Breakpoint): Thenable<DebugProtocolBreakpoint | undefined>;
}

export interface DebugSessionCustomEvent {
  readonly session: DebugSession;
  readonly event: string;
  readonly body: any;
}

export namespace debug {
  export const onDidStartDebugSession: Event<DebugSession> = new EventEmitter<DebugSession>().event;
  export const onDidTerminateDebugSession: Event<DebugSession> = new EventEmitter<DebugSession>().event;
  export const onDidReceiveDebugSessionCustomEvent: Event<DebugSessionCustomEvent> = new EventEmitter<DebugSessionCustomEvent>().event;
}
