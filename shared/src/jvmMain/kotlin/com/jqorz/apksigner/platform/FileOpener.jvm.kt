package com.jqorz.apksigner.platform

import java.awt.Desktop
import java.io.File

actual fun openInFileExplorer(path: String) {
    try {
        val file = File(path)
        // 若为文件则打开其所在目录
        val dir = if (file.isFile) file.parentFile ?: file else file
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(dir)
        } else {
            // 兜底：直接调用 explorer 打开目录
            Runtime.getRuntime().exec(arrayOf("explorer.exe", dir.absolutePath))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
