// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline

import com.hulylabs.intellij.plugins.cline.nodejs.ClineRuntimeService
import com.intellij.ide.CliResult
import com.intellij.ide.ProtocolHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager

private val LOG = Logger.getInstance("#cline.protocol")
class HulyCodeProtocolHandler: ProtocolHandler {
  override val scheme: String get() = "huly-code"

  override suspend fun process(query: String): CliResult {
    LOG.info("HulyCodeProtocolHandler.process($query)")
    if (query.startsWith("saoudrizwan.claude-dev/")) {
      val project = ProjectManager.getInstance().openProjects.first()
      var query = query.substringAfter("saoudrizwan.claude-dev/")
      if (query.startsWith("auth?")) {
        ClineRuntimeService.getInstance(project).clineAuthResponse(query.substringAfter("auth?"))
      }
    }
    return CliResult.OK
  }
}