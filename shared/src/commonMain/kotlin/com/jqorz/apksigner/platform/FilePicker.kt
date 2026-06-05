package com.jqorz.apksigner.platform

interface FilePicker {
    /**
     * 选择APK文件
     * @return 选中的文件路径，null表示取消
     */
    fun selectApkFile(): String?

    /**
     * 选择keystore文件
     * @return 选中的文件路径，null表示取消
     */
    fun selectKeyStoreFile(): String?

    /**
     * 选择apksigner可执行文件
     * @return 选中的文件路径，null表示取消
     */
    fun selectApkSignerFile(): String?

    /**
     * 选择文件夹
     * @return 选中的文件夹路径，null表示取消
     */
    fun selectDirectory(): String?
}

expect fun createFilePicker(): FilePicker
