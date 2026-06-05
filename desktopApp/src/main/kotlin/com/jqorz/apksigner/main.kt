package com.jqorz.apksigner

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import java.awt.Dimension

fun main() {
    // 设置系统外观，让 Swing 组件使用 Windows 原生风格
    try {
        javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())
    } catch (_: Exception) { }

    application {
        val minWidth = 1080.dp
        val minHeight = 720.dp

        Window(
            onCloseRequest = ::exitApplication,
            title = "ApkSigner",
            state = WindowState(
                size = DpSize(minWidth, minHeight)
            ),
        ) {
            // 将 dp 转换为像素设置最小尺寸
            val density = window.graphicsConfiguration.defaultTransform.scaleX.toFloat()
            window.minimumSize = Dimension(
                (minWidth.value * density).toInt(),
                (minHeight.value * density).toInt()
            )
            App()
        }
    }
}