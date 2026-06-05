package com.jqorz.apksigner.model

data class SignConfig(
    val apkPath: String,
    val keyStoreInfo: KeyStoreInfo,
    val signSchemes: Set<SignScheme>,
    val outputApkPath: String? = null
)

enum class SignScheme(val flag: String, val displayName: String) {
    V1("--v1-signing-enabled", "V1 (JAR签名)"),
    V2("--v2-signing-enabled", "V2 (APK签名方案v2)"),
    V3("--v3-signing-enabled", "V3 (APK签名方案v3)"),
    V4("--v4-signing-enabled", "V4 (APK签名方案v4)")
}

sealed class SigningState {
    data object Idle : SigningState()
    data class Signing(val message: String) : SigningState()
    data class Success(val outputPath: String) : SigningState()
    data class Error(val message: String) : SigningState()
}
