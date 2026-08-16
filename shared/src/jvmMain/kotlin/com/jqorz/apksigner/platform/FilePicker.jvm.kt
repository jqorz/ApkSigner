package com.jqorz.apksigner.platform

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class JVMFilePicker : FilePicker {

    override fun selectApkFile(initialDirectory: String?): String? {
        return openFileDialog(
            title = "选择APK文件",
            extensions = listOf("apk"),
            description = "APK文件",
            initialDirectory = initialDirectory
        )
    }

    override fun selectKeyStoreFile(): String? {
        return openFileDialog(
            title = "选择密钥库文件",
            extensions = listOf("jks", "keystore", "p12", "pfx"),
            description = "密钥库文件"
        )
    }

    override fun selectApkSignerFile(): String? {
        return openFileDialog(
            title = "选择apksigner可执行文件",
            extensions = listOf("bat", "cmd", "exe"),
            description = "apksigner文件"
        )
    }

    override fun selectDirectory(): String? {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "选择目录"
        }
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
    }

    private fun openFileDialog(
        title: String,
        extensions: List<String>,
        description: String,
        initialDirectory: String? = null
    ): String? {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            val extArray = extensions.toTypedArray()
            fileFilter = FileNameExtensionFilter(
                "$description (${extArray.joinToString(", ") { "*.$it" }})",
                *extArray
            )
            dialogTitle = title
            // 定位到初始目录（若传入的是文件路径则定位到其所在目录）
            if (initialDirectory != null) {
                val dir = File(initialDirectory).let { if (it.isFile) it.parentFile else it }
                if (dir != null && dir.isDirectory) {
                    currentDirectory = dir
                }
            }
        }
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
    }
}

actual fun createFilePicker(): FilePicker = JVMFilePicker()
