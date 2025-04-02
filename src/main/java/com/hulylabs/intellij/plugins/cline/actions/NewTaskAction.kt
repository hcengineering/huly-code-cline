// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.actions

import com.hulylabs.intellij.plugins.cline.nodejs.ClineRuntimeService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.IconLoader

class NewTaskAction() : AnAction("New Task", null, IconLoader.getIcon("/icons/add.svg", NewTaskAction::class.java)) {
  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val clineRuntimeService = ClineRuntimeService.getInstance(e.project!!)
    clineRuntimeService.newChat()
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }
}