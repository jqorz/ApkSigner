package com.jqorz.apksigner

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.jetbrains.skia.Image
import java.awt.Dimension

fun main() {
    // 设置系统外观，让 Swing 组件使用 Windows 原生风格
    try {
        javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())
    } catch (_: Exception) { }

    application {
        val minWidth = 1080.dp
        val minHeight = 720.dp

        // 加载应用图标
        val icon = remember {
            try {
                val iconStream = object {}.javaClass.getResourceAsStream("/app_icon.png")
                iconStream?.let { stream ->
                    val bytes = stream.readBytes()
                    val skiaImage = Image.makeFromEncoded(bytes)
                    BitmapPainter(skiaImage.toComposeImageBitmap())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "ApkSigner",
            state = WindowState(
                size = DpSize(minWidth, minHeight)
            ),
            icon = icon,
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