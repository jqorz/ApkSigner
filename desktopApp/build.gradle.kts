import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.core.Environment.Companion.application

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
    application {
        mainClass = "com.jqorz.apksigner.MainKt"

        // Release 构建启用 ProGuard 裁剪/混淆，显著缩小安装包
        buildTypes.release.proguard {
            configurationFiles.from("rules.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "ApkSigner"
            // 注意: 每次发新版本必须递增此版本号, 否则MSI会因"同版本禁止覆盖安装"导致安装exe闪退
            packageVersion = "1.0.1"

            // Windows ICO 图标
            windows {
                // 升级标识UUID, 固定不变, 用于让新版本安装包能识别并覆盖旧版本
                upgradeUuid = "3de1d7f7-fc58-4493-ae65-4c3cffd351b9"
                iconFile.set(project.file("src/main/resources/app_icon.ico"))
                menuGroup = "ApkSigner"
                shortcut = true
                dirChooser = true
            }

            // macOS 图标 (可选，使用 PNG)
            macOS {
                iconFile.set(project.file("src/main/resources/app_icon.png"))
            }

            // Linux 图标 (可选)
            linux {
                iconFile.set(project.file("src/main/resources/app_icon.png"))
            }
        }
    }
}