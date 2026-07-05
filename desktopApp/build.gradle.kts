import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.jqorz.apksigner.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ApkSigner"
            packageVersion = "1.0.0"

            // Windows ICO 图标
            windows {
                iconFile.set(project.file("src/main/resources/app_icon.ico"))
                menuGroup = "AndDevHelper"
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