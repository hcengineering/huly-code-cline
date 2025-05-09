// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.vscode;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.hulylabs.intellij.plugins.cline.nodejs.ClineRuntimeService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class WebView implements Disposable {
  private final Project project;
  private final NodeRuntime nodeRuntime;
  private final JBCefBrowser browser;
  private final Logger LOG = Logger.getInstance("#cline.webView");
  private String html;
  private V8Value options;
  private JBCefJSQuery postMessageQuery;
  private JBCefJSQuery getStateQuery;
  private JBCefJSQuery setStateQuery;
  private V8ValueFunction onDidReceiveMessageListener;

  public WebView(Project project, NodeRuntime nodeRuntime, JBCefBrowser browser) {
    this.project = project;
    this.nodeRuntime = nodeRuntime;
    this.browser = browser;
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
    if (browser != null) {
      postMessageQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
      setStateQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
      getStateQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);

      postMessageQuery.addHandler((jsonStr) -> {
        if (jsonStr.contains("openExtensionSettings")) {
          ApplicationManager.getApplication().invokeLater(() -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Cline");
          });
          return null;
        }
        ClineRuntimeService.getInstance(project).addMessage(onDidReceiveMessageListener, jsonStr);
        return null;
      });

      setStateQuery.addHandler((json) -> {
        return null;
      });

      getStateQuery.addHandler((json) -> {
        return null;
      });

      ApplicationManager.getApplication().invokeLater(() -> {
        browser.loadURL("http://hulycline/index.html");
        String jsCode =
          "acquireVsCodeApi = function() {\n" +
          "  return {\n" +
          "    postMessage: function(message) { \n" +
          "      " + postMessageQuery.inject("JSON.stringify(message)") + "\n" +
          "    },\n" +
          "    getState: function() { \n" +
          "      var t = " + getStateQuery.inject("") + "\n" +
          "      return t;\n" +
          "    },\n" +
          "    setState: function(newState) { \n" +
          "      " + setStateQuery.inject("JSON.stringify(newState)") + "\n" +
          "      \n" +
          "    }\n" +
          "  };\n" +
          "};";
        browser.getCefBrowser().executeJavaScript(jsCode, browser.getCefBrowser().getURL(), 0);
      });
    }
  }

  public V8Value getOptions() {
    return options;
  }

  public void setOptions(V8Value options) {
    this.options = options;
  }

  public String getCspSource() {
    return "http://hulycline/";
  }

  public void postMessage(V8ValueObject message) {
    final AtomicReference<String> msgHolder = new AtomicReference<>("");
    if (browser != null) {
      try {
        String msg = nodeRuntime.getGlobalObject().getBuiltInJson().stringify(message);
        //if (msg.length() > 2048) {
        //  System.out.println("!!!!! postMessage " + msg.substring(0, 2048));
        //}
        //else {
        //  System.out.println("!!!!! postMessage " + msg);
        //}
        msgHolder.set(msg);
      }
      catch (JavetException e) {
        LOG.error(e);
      }
    }
    ApplicationManager.getApplication().invokeLater(() -> {
      assert browser != null;
      browser.getCefBrowser().executeJavaScript(
        String.format("window.postMessage(%s, \"*\");", msgHolder.get()),
        browser.getCefBrowser().getURL(),
        0
      );
    });
  }


  public void onDidReceiveMessage(V8ValueFunction listener, V8Value args, V8Value disposable) {
    onDidReceiveMessageListener = listener;
    try {
      onDidReceiveMessageListener.setWeak();
      nodeRuntime.getGlobalObject().set("onDidReceiveMessageListener", listener);
    }
    catch (JavetException e) {
      LOG.error(e);
    }
  }

  public V8ValueObject asWebviewUri(@NotNull V8ValueObject uri) {
    return uri;
  }

  @Override
  public void dispose() {
    try {
      if (onDidReceiveMessageListener != null && !onDidReceiveMessageListener.isClosed()) {
        onDidReceiveMessageListener.clearWeak();
      }
    }
    catch (JavetException e) {
      LOG.error(e);
    }
  }
}
