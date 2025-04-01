// Copyright © 2025 Huly Labs. Use of this source code is governed by the Apache 2.0 license.
package com.hulylabs.intellij.plugins.cline.nodejs

import com.hulylabs.intellij.plugins.langconfigurator.utils.DecompressUtils
import com.hulylabs.intellij.plugins.langconfigurator.utils.DownloadUtils
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.system.CpuArch
import java.nio.file.Files
import java.nio.file.Path

private const val VERSION = "14.1.1"
private const val DOWNLOAD_URL_PREFIX = "https://github.com/BurntSushi/ripgrep/releases/download/${VERSION}/"

class RipGrepRuntime {
  companion object {
    @JvmStatic
    private fun getBinaryDir(): Path {
      return Path.of(PathManager.getSystemPath(), "cline", "node_modules", "vscode-ripgrep", "bin")
    }

    private fun getBinaryName(): String {
      return if (SystemInfo.isWindows) "rg.exe" else "rg"
    }

    private fun getBinaryPath(): Path {
      return getBinaryDir().resolve(getBinaryName())
    }

    private fun getDownloadFileName(): String {
      val arch = if (CpuArch.isArm64()) "aarch64" else "x86_64"
      val os = if (SystemInfo.isWindows) "pc-windows-msvc" else (if (SystemInfo.isMac) "apple-darwin" else (if (SystemInfo.isLinux) "unknown-linux-musl" else ""))
      return "ripgrep-${VERSION}-$arch-$os"
    }

    @JvmStatic
    suspend fun init() {
      if (getBinaryPath().toFile().exists()) {
        return
      }
      val downloadExtension = if (SystemInfo.isWindows) ".zip" else ".tar.gz"
      val downloadFileName = getDownloadFileName() + downloadExtension
      val downloadUrl = DOWNLOAD_URL_PREFIX + downloadFileName
      DownloadUtils.downloadFile(downloadUrl, downloadFileName, getBinaryDir().toFile())
      DecompressUtils.decompress(getBinaryDir().resolve(downloadFileName), getBinaryDir().parent)
      Files.move(getBinaryDir().parent.resolve(getDownloadFileName()).resolve(getBinaryName()), getBinaryPath())
      FileUtil.deleteRecursively(getBinaryDir().parent.resolve(getDownloadFileName()))
      if (!SystemInfo.isWindows) {
        FileUtil.setExecutable(getBinaryPath().toFile())
      }
    }
  }
}