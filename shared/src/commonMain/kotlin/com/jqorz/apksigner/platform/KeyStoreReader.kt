package com.jqorz.apksigner.platform

/**
 * 读取 keystore 文件中的别名列表
 */
interface KeyStoreReader {
    /**
     * 获取 keystore 文件中的所有别名
     * @param filePath keystore 文件路径
     * @param storePassword 密钥库密码
     * @param storeType 密钥库类型 (JKS, PKCS12)
     * @return 别名列表，如果密码错误或文件无效返回空列表
     */
    fun getAliases(filePath: String, storePassword: String, storeType: String): List<String>
}

expect fun createKeyStoreReader(): KeyStoreReader
