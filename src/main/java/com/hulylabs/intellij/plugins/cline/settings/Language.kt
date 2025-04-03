// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.settings

enum class Language(val displayName: String) {
  ENGLISH("English"),
  ARABIC("Arabic - العربية"),
  PORTUGUESE("Portuguese - Português (Brasil)"),
  CZECH("Czech - Čeština"),
  FRENCH("French - Français"),
  GERMAN("German - Deutsch"),
  HINDI("Hindi - हिन्दी"),
  HUNGARIAN("Hungarian - Magyar"),
  ITALIAN("Italian - Italiano"),
  JAPANESE("Japanese - 日本語"),
  KOREAN("Korean - 한국어"),
  POLISH("Polish - Polski"),
  PORTUGUESE_BRAZIL("Portuguese - Português (Portugal)"),
  RUSSIAN("Russian - Русский"),
  SIMPLIFIED_CHINESE("Simplified Chinese - 简体中文"),
  SPANISH("Spanish - Español"),
  TRADITIONAL_CHINESE("Traditional Chinese - 繁體中文"),
  TURKISH("Turkish - Türkçe");

  companion object {
    fun forName(name: String) = McpMode.entries.find{ it.displayName == name }
  }
}
