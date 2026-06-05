package com.jqorz.apksigner.platform

import java.io.FileInputStream
import java.security.KeyStore

class JVMKeyStoreReader : KeyStoreReader {
    override fun getAliases(filePath: String, storePassword: String, storeType: String): List<String> {
        return try {
            val ksType = when (storeType.uppercase()) {
                "PKCS12" -> "PKCS12"
                else -> "JKS"
            }
            val ks = KeyStore.getInstance(ksType)
            FileInputStream(filePath).use { fis ->
                ks.load(fis, storePassword.toCharArray())
            }
            ks.aliases().toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

actual fun createKeyStoreReader(): KeyStoreReader = JVMKeyStoreReader()
