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
            packageVersion = "1.0.0"

            // Windows ICO 图标
            windows {
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