import * as vscode from "vscode"

export async function testTerminal() {
  const terminal = vscode.window.createTerminal({
    cwd: "f:\\work\\_misc\\pexport2",
    name: "Cline",
    iconPath: new vscode.ThemeIcon("robot"),
  });
  await new Promise(f => setTimeout(f, 1000));

  var resp = terminal.shellIntegration?.executeCommand?.("cat Cargo.toml");
  if (resp) {
    var items = resp.read()
    for await (const line of items) {
      console.log("!!!line :", line);
    }
    console.log("!!!!finished")
  }
}