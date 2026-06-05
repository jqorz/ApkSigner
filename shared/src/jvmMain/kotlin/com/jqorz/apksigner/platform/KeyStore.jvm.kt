package com.jqorz.apksigner.platform

import com.jqorz.apksigner.model.KeyStoreInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.SecureRandom
import java.util.Base64
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class JVMKeyStoreStorage : KeyStoreStorage {
    private val prefs = Preferences.userNodeForPackage(JVMKeyStoreStorage::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    private val keyFile = File(System.getProperty("user.home"), ".apksigner_key")

    private fun getOrCreateSecretKey(): ByteArray {
        val existing = prefs.get("encryption_key", null)
        if (existing != null) {
            return Base64.getDecoder().decode(existing)
        }
        val key = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.put("encryption_key", Base64.getEncoder().encodeToString(key))
        return key
    }

    override fun encryptPassword(password: String): String {
        val key = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    override fun decryptPassword(encrypted: String): String {
        val key = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted))
        return String(decrypted, Charsets.UTF_8)
    }

    override fun saveKeyStore(info: KeyStoreInfo) {
        val allKeys = getAllKeyStores().toMutableList()
        val index = allKeys.indexOfFirst { it.id == info.id }
        if (index >= 0) {
            allKeys[index] = info
        } else {
            allKeys.add(info)
        }
        prefs.put(KEYSTORE_PREFS_KEY, json.encodeToString(allKeys))
    }

    override fun getAllKeyStores(): List<KeyStoreInfo> {
        val data = prefs.get(KEYSTORE_PREFS_KEY, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<KeyStoreInfo>>(data)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun deleteKeyStore(id: String) {
        val allKeys = getAllKeyStores().toMutableList()
        allKeys.removeAll { it.id == id }
        prefs.put(KEYSTORE_PREFS_KEY, json.encodeToString(allKeys))
    }

    companion object {
        private const val KEYSTORE_PREFS_KEY = "saved_keystores"
    }
}

actual fun createKeyStoreStorage(): KeyStoreStorage = JVMKeyStoreStorage()
