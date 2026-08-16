# ProGuard 规则（release 打包用）

# 入口类
-keep class com.jqorz.apksigner.MainKt { *; }

# ---- kotlinx.serialization（AppSettings / KeyStoreInfo）----
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.jqorz.apksigner.**$$serializer { *; }
-keepclassmembers class com.jqorz.apksigner.** {
    *** Companion;
}
-keepclasseswithmembers class com.jqorz.apksigner.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Compose / Skiko ----
# Compose 运行时通过反射读取部分工具信息
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }
# Skiko 本地库加载
-keep class org.jetbrains.skiko.** { *; }
-dontwarn org.jetbrains.skiko.**

# Swing/AWT 交互（coroutines-swing 通过反射查找 Dispatchers）
-keepnames class kotlinx.coroutines.swing.Swing
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
