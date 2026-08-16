package com.jqorz.apksigner.platform

interface FilePicker {
    /**
     * 选择APK文件
     * @param initialDirectory 文件对话框的初始目录（可为上一次选择的APK所在目录），null使用默认位置
     * @return 选中的文件路径，null表示取消
     */
    fun selectApkFile(initialDirectory: String? = null): String?

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
