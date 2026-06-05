package com.jqorz.apksigner.platform

import com.jqorz.apksigner.model.KeyStoreInfo

interface KeyStoreStorage {
    /**
     * 保存密钥信息
     */
    fun saveKeyStore(info: KeyStoreInfo)

    /**
     * 获取所有保存的密钥信息
     */
    fun getAllKeyStores(): List<KeyStoreInfo>

    /**
     * 根据ID删除密钥信息
     */
    fun deleteKeyStore(id: String)

    /**
     * 加密密码
     */
    fun encryptPassword(password: String): String

    /**
     * 解密密码
     */
    fun decryptPassword(encrypted: String): String
}

expect fun createKeyStoreStorage(): KeyStoreStorage
