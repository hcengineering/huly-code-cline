// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs.cdt

object CDTConfig {
  val COMMAND_EXIT: String = ".exit"
  val PATH_ROOT: String = "/"
  val PATH_JSON: String = "/json"
  val PATH_JSON_LIST: String = "/json/list"
  val PATH_JSON_VERSION: String = "/json/version"
  val PATH_JAVET: String = "/javet"
  var port: Int = 9229

  fun getWebSocketUrl(): String {
    return "localhost:$port$PATH_JAVET"
  }
}