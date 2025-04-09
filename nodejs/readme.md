### How build Cline dependencies

To build Cline dependecies you need perform the following steps:
1. Checkout Cline repository into `cline` folder (see prepare.cmd for details)
2. Check cline\package.json for node dependecies and sync them with package.json in nodejs folder
3. Run `npm install` in nodejs folder for update nodejs dependecies
4. Copy `node_modules\@vscode\codicons` into `cline\node_modules\@vscode\codicons`
5. Run `npm run build:webview` in nodejs folder for build webview
6. Copy `cline\webview-ui\build\assets` into `..\webview-cline\assets`
7. Run `node esbuild.js` in nodejs folder for build Cline nodejs runtime, artifacts will be placed in `..\src\main\resources\nodejs\runtime`

To check that all VsCode dependecies correctly resolved run `tsc` in nodejs directory