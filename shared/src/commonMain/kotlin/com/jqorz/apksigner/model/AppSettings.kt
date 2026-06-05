package com.jqorz.apksigner.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val apksignerPath: String? = null,
    val androidSdkPath: String? = null,
    val lastApkPath: String? = null,
    val lastKeyStoreId: String? = null
)
