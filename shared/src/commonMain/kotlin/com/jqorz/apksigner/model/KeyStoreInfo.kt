package com.jqorz.apksigner.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyStoreInfo(
    val id: String,
    val name: String,
    val filePath: String,
    val storePasswordEncrypted: String,
    val keyAlias: String,
    val keyPasswordEncrypted: String,
    val storeType: StoreType,
    val createdAt: Long
)

@Serializable
enum class StoreType {
    JKS,
    PKCS12
}
