package com.jqorz.apksigner.platform

import com.jqorz.apksigner.model.SignConfig
import com.jqorz.apksigner.model.SignScheme
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class JVMApkSignerTool : ApkSignerTool {
    override fun detectApkSigner(): String? {
        // 尝试从环境变量ANDROID_HOME查找
        val androidHome = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        if (androidHome != null) {
            val apksignerPath = findApkSignerInSdk(androidHome)
            if (apksignerPath != null) return apksignerPath
        }

        // 尝试从PATH中查找
        val pathSeparator = File.pathSeparator
        val paths = System.getenv("PATH")?.split(pathSeparator) ?: emptyList()
        for (path in paths) {
            val apksigner = File(path, "apksigner")
            val apksignerBat = File(path, "apksigner.bat")
            if (apksigner.exists() || apksignerBat.exists()) {
                return if (apksigner.exists()) apksigner.absolutePath else apksignerBat.absolutePath
            }
        }

        // 尝试常见的SDK安装路径
        val commonPaths = listOf(
            "${System.getProperty("user.home")}/AppData/Local/Android/Sdk",
            "${System.getProperty("user.home")}/Android/Sdk",
            "C:/Android/Sdk",
            "C:/Users/${System.getProperty("user.name")}/AppData/Local/Android/Sdk"
        )
        for (sdkPath in commonPaths) {
            val apksignerPath = findApkSignerInSdk(sdkPath)
            if (apksignerPath != null) return apksignerPath
        }

        return null
    }

    private fun findApkSignerInSdk(sdkPath: String): String? {
        val buildToolsDir = File(sdkPath, "build-tools")
        if (!buildToolsDir.exists()) return null

        // 获取最新版本的build-tools
        val versions = buildToolsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.name }
            ?: return null

        for (version in versions) {
            val apksigner = File(version, "apksigner.bat")
            val apksignerSh = File(version, "apksigner")
            if (apksigner.exists()) return apksigner.absolutePath
            if (apksignerSh.exists()) return apksignerSh.absolutePath
        }
        return null
    }

    override fun sign(
        config: SignConfig,
        apksignerPath: String,
        onProgress: (String) -> Unit
    ): SignResult {
        val keystoreStorage = createKeyStoreStorage()
        val storePassword = keystoreStorage.decryptPassword(config.keyStoreInfo.storePasswordEncrypted)
        val keyPassword = keystoreStorage.decryptPassword(config.keyStoreInfo.keyPasswordEncrypted)

        val outputApk = config.outputApkPath ?: generateSignedApkPath(config.apkPath)

        // 构建命令参数
        // 优先使用 apksigner.jar 直接执行，避免 .bat 文件的参数解析问题
        val apksignerDir = File(apksignerPath).parentFile
        val apksignerJar = File(apksignerDir, "lib/apksigner.jar")

        val command = mutableListOf<String>()

        if (apksignerJar.exists()) {
            // 使用 java -jar apksigner.jar
            println("[ApkSigner] 使用 JAR 文件: $apksignerJar")
            command.addAll(listOf(
                "java", "-jar", apksignerJar.absolutePath,
                "sign",
                "--ks", config.keyStoreInfo.filePath,
                "--ks-pass", "pass:$storePassword",
                "--ks-key-alias", config.keyStoreInfo.keyAlias,
                "--key-pass", "pass:$keyPassword"
            ))
        } else {
            // 降级到 bat 文件
            println("[ApkSigner] 使用 BAT 文件: $apksignerPath")
            command.addAll(listOf(
                apksignerPath,
                "sign",
                "--ks", config.keyStoreInfo.filePath,
                "--ks-pass", "pass:$storePassword",
                "--ks-key-alias", config.keyStoreInfo.keyAlias,
                "--key-pass", "pass:$keyPassword"
            ))
        }

        // 添加签名方案选项
        for (scheme in SignScheme.entries) {
            command.add(scheme.flag)
            command.add(if (config.signSchemes.contains(scheme)) "true" else "false")
        }

        // --out 和输入 APK 必须在最后
        command.addAll(listOf(
            "--out", outputApk,
            config.apkPath
        ))

        return try {
            onProgress("正在执行签名命令...")
            // 打印命令用于调试（隐藏密码）
            val debugCommand = command.map { arg ->
                when {
                    arg.startsWith("pass:") -> "pass:***"
                    else -> arg
                }
            }
            val cmdStr = debugCommand.joinToString(" ")
            println("[ApkSigner] 执行命令: $cmdStr")
            onProgress("命令: $cmdStr")

            val processBuilder = ProcessBuilder(command)
            // 设置工作目录为 APK 文件所在目录
            val workingDir = File(config.apkPath).parentFile
            if (workingDir != null && workingDir.exists()) {
                processBuilder.directory(workingDir)
                println("[ApkSigner] 工作目录: $workingDir")
            }
            val process = processBuilder.start()

            // 分别读取 stdout 和 stderr
            val stdout = process.inputStream.bufferedReader()
            val stderr = process.errorStream.bufferedReader()

            val output = StringBuilder()
            val errorOutput = StringBuilder()

            // 读取 stdout
            val stdoutThread = Thread {
                stdout.forEachLine { line ->
                    output.appendLine(line)
                    println("[stdout] $line")
                    onProgress(line)
                }
            }

            // 读取 stderr
            val stderrThread = Thread {
                stderr.forEachLine { line ->
                    errorOutput.appendLine(line)
                    println("[stderr] $line")
                    onProgress("[ERROR] $line")
                }
            }

            stdoutThread.start()
            stderrThread.start()

            stdoutThread.join()
            stderrThread.join()

            val exitCode = process.waitFor()
            println("[ApkSigner] 进程退出码: $exitCode")
            if (exitCode == 0) {
                println("[ApkSigner] 签名成功: $outputApk")
                onProgress("签名完成!")
                SignResult(
                    success = true,
                    outputPath = outputApk,
                    error = null
                )
            } else {
                println("[ApkSigner] 签名失败, exitCode=$exitCode")
                val errorMsg = buildString {
                    appendLine("签名失败 (exit code: $exitCode)")
                    if (errorOutput.isNotBlank()) {
                        appendLine("错误输出:")
                        appendLine(errorOutput.toString().trim())
                    }
                    if (output.isNotBlank()) {
                        appendLine("标准输出:")
                        appendLine(output.toString().trim())
                    }
                }
                SignResult(
                    success = false,
                    outputPath = null,
                    error = errorMsg
                )
            }
        } catch (e: Exception) {
            SignResult(
                success = false,
                outputPath = null,
                error = "执行签名时出错: ${e.javaClass.simpleName}: ${e.message}"
            )
        }
    }

    private fun generateSignedApkPath(apkPath: String): String {
        val file = File(apkPath)
        val name = file.nameWithoutExtension
        val ext = file.extension
        val parent = file.parentFile
        return File(parent, "${name}_signed.$ext").absolutePath
    }
}

actual fun createApkSignerTool(): ApkSignerTool = JVMApkSignerTool()
