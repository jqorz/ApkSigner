package com.jqorz.apksigner.platform

import com.jqorz.apksigner.model.SignConfig

interface ApkSignerTool {
    /**
     * 检测apksigner是否可用
     * @return apksigner路径，null表示不可用
     */
    fun detectApkSigner(): String?

    /**
     * 执行APK签名
     * @param config 签名配置
     * @param apksignerPath apksigner工具路径
     * @param onProgress 进度回调
     * @return 签名结果
     */
    fun sign(
        config: SignConfig,
        apksignerPath: String,
        onProgress: (String) -> Unit
    ): SignResult
}

data class SignResult(
    val success: Boolean,
    val outputPath: String?,
    val error: String?
)

expect fun createApkSignerTool(): ApkSignerTool
