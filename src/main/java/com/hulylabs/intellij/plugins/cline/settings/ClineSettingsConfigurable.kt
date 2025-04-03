// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JList

class ClineSettingsConfigurable : Configurable {
  private val settings = ClineConfiguration.getInstance()
  private var workspaceSettings = settings.workspaceParams
  private lateinit var chromePathField: TextFieldWithBrowseButton
  private lateinit var disableBrowserCheckbox: JBCheckBox
  private lateinit var enableCheckpointsCheckbox: JBCheckBox
  private lateinit var mcpModeCombo: ComboBox<McpMode>
  private lateinit var mcpMarketplaceCheckbox: JBCheckBox
  private lateinit var reasoningEffortsCombo: ComboBox<ReasoningEffort>
  private lateinit var preferredLanguageCombo: ComboBox<Language>

  override fun getDisplayName(): String = "Cline Settings"

  override fun createComponent(): JComponent {
    return panel {
      group("Workspace Settings") {
        row {
          checkBox("Enable checkpoints")
            .comment(
              "Enables extension to save checkpoints of workspace throughout the task. " +
              "Uses git under the hood which may not work well with large workspaces."
            )
            .applyToComponent { enableCheckpointsCheckbox = this }
        }
      }

      group("Browser Settings") {
        row("Chrome executable path:") {
          chromePathField = textFieldWithBrowseButton("Select Chrome Executable", fileChosen = {
            return@textFieldWithBrowseButton it.path
          }).comment(
            "Path to Chrome executable for browser use functionality. If not set, the extension will attempt to find or download it automatically."
          ).component
        }
        row {
          checkBox("Disable browser tool")
            .comment("Disables extension from spawning browser session.")
            .applyToComponent { disableBrowserCheckbox = this }
        }
      }

      group("MCP Settings") {
        row("MCP mode:") {
          comboBox(McpMode.entries)
            .comment(
              "Controls MCP inclusion in prompts, reduces token usage if you only need access to certain functionality."
            )
            .applyToComponent {
              mcpModeCombo = this
              this.renderer = object : SimpleListCellRenderer<McpMode>() {
                override fun customize(list: JList<out McpMode>, value: McpMode?, index: Int, selected: Boolean, hasFocus: Boolean) {
                  text = value?.displayName ?: ""
                  toolTipText = value?.description
                }
              }
            }
        }
        row {
          checkBox("Enable MCP marketplace")
            .comment("Controls whether the MCP Marketplace is enabled.")
            .applyToComponent { mcpMarketplaceCheckbox = this }
        }
      }
      group("Model Settings") {
        row("O3 Mini: Reasoning effort") {
          comboBox(ReasoningEffort.entries,
                   SimpleListCellRenderer.create("") { value -> value.displayName })
            .comment(
              "Controls the reasoning effort when using the o3-mini model. Higher values may result in more thorough but slower responses."
            )
            .applyToComponent { reasoningEffortsCombo = this }
        }
        row("PreferredLanguage") {
          comboBox(Language.entries,
                   SimpleListCellRenderer.create("") { value -> value.displayName })
            .comment(
              "The language that Cline should use for communication."
            )
            .applyToComponent { preferredLanguageCombo = this }
        }
      }
    }
  }

  override fun isModified(): Boolean {
    return chromePathField.text != settings.get("cline.chromeExecutablePath") ||
           disableBrowserCheckbox.isSelected != settings.get("cline.disableBrowserTool").toBoolean() ||
           enableCheckpointsCheckbox.isSelected != settings.get("cline.enableCheckpoints").toBoolean() ||
           mcpModeCombo.selectedItem != McpMode.forName(settings.get("cline.mcp.mode")) ||
           mcpMarketplaceCheckbox.isSelected != settings.get("cline.mcpMarketplace.enabled").toBoolean() ||
           preferredLanguageCombo.selectedItem != Language.forName(settings.get("cline.preferredLanguage")) ||
           reasoningEffortsCombo.selectedItem != ReasoningEffort.forName(settings.get("cline.modelSettings.o3Mini.reasoningEffort"))
  }

  override fun apply() {
    workspaceSettings.put("cline.chromeExecutablePath", chromePathField.text)
    workspaceSettings.put("cline.disableBrowserTool", disableBrowserCheckbox.isSelected.toString())
    workspaceSettings.put("cline.enableCheckpoints", enableCheckpointsCheckbox.isSelected.toString())
    workspaceSettings.put("cline.mcp.mode", (mcpModeCombo.selectedItem as McpMode).displayName)
    workspaceSettings.put("cline.mcpMarketplace.enabled", mcpMarketplaceCheckbox.isSelected.toString())
    workspaceSettings.put("cline.preferredLanguage", (preferredLanguageCombo.selectedItem as Language).displayName)
    workspaceSettings.put("cline.modelSettings.o3Mini.reasoningEffort", (reasoningEffortsCombo.selectedItem as ReasoningEffort).displayName)
  }

  override fun reset() {
    chromePathField.text = settings.get("cline.chromeExecutablePath")
    disableBrowserCheckbox.isSelected = settings.get("cline.disableBrowserTool").toBoolean()
    enableCheckpointsCheckbox.isSelected = settings.get("cline.enableCheckpoints").toBoolean()
    mcpModeCombo.selectedItem = McpMode.forName(settings.get("cline.mcp.mode"))
    mcpMarketplaceCheckbox.isSelected = settings.get("cline.mcpMarketplace.enabled").toBoolean()
    preferredLanguageCombo.selectedItem = Language.forName(settings.get("cline.preferredLanguage"))
    reasoningEffortsCombo.selectedItem = ReasoningEffort.forName(settings.get("cline.modelSettings.o3Mini.reasoningEffort"))
  }
}