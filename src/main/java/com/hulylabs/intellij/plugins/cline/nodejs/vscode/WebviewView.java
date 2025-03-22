// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode;

import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefBrowser;

public class WebviewView implements Disposable {
  private Webview webview;

  public WebviewView(Project project, NodeRuntime nodeRuntime, JBCefBrowser browser) {
    webview = new Webview(project, nodeRuntime, browser);
  }

  public void onDidChangeVisibility(V8ValueFunction listener, V8Value args, V8Value[] disposables) {
    System.out.println("!!!!! onDidChangeVisibility");
    return;
  }

  public Object onDidDispose(V8ValueFunction listener, V8Value args, V8Value[] disposables) {
    System.out.println("!!!!! onDidDispose");
    return null;
  }

  public Webview getWebview() {
    return webview;
  }

  @Override
  public void dispose() {
    Disposer.dispose(webview);
  }
}
