package com.jqorz.apksigner.platform

/**
 * 在系统文件管理器中打开指定路径所在的目录
 * @param path 文件或目录路径；若为文件，则打开其所在目录
 */
expect fun openInFileExplorer(path: String)
