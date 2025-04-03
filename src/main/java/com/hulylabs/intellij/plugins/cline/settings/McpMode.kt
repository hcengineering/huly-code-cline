// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.settings

// Enum for MCP Mode
enum class McpMode(
  val displayName: String,
  val description: String,
) {
  FULL(
    "full",
    "Enable all MCP functionality (server use and build instructions)"
  ),
  SERVER_USE_ONLY(
    "server-use-only",
    "Enable MCP server use only (excludes instructions about building MCP servers)"
  ),
  OFF(
    "off",
    "Disable all MCP functionality"
  );

  companion object {
    fun forName(name: String) = McpMode.entries.find{ it.displayName == name }
  }
}