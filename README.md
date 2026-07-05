# APK签名工具

基于 Kotlin Multiplatform + Compose Desktop 开发的 APK 签名工具，支持调用 Android SDK 的 apksigner 进行签名。

## 主要功能

- **APK签名** - 支持对 APK 文件进行签名
- **多签名方案** - 支持 V1 (JAR签名)、V2、V3、V4 签名方案
- **密钥管理** - 支持保存、编辑、删除密钥信息，无需每次重新输入
- **密码加密存储** - 密钥密码使用 AES 加密存储，安全可靠
- **别名自动读取** - 输入密码后自动从 keystore 文件读取别名列表
- **apksigner 自动检测** - 自动从 ANDROID_HOME 环境变量查找 apksigner 工具
- **手动配置路径** - 支持手动指定 apksigner 或 Android SDK 路径
- **签名日志** - 实时显示签名过程日志，支持复制

## 环境要求

- JDK 17+
- Android SDK（需要 build-tools 中的 apksigner）

## 运行方式

### 使用 Gradle 命令

```bash
# 标准运行
./gradlew :desktopApp:run
```

### 使用 IDE

直接运行 `desktopApp/src/main/kotlin/com/jqorz/apksigner/main.kt` 中的 `main()` 函数。

## 项目结构

```
ApkSigner/
├── shared/                          # 共享模块
│   └── src/
│       ├── commonMain/              # 通用代码
│       │   ├── kotlin/com/jqorz/apksigner/
│       │   │   ├── model/           # 数据模型
│       │   │   ├── platform/        # 平台接口
│       │   │   ├── ui/              # UI组件
│       │   │   └── viewmodel/       # 视图模型
│       │   └── composeResources/    # 资源文件
│       └── jvmMain/                 # JVM平台实现
│           └── kotlin/com/jqorz/apksigner/
│               └── platform/        # 平台相关实现
├── desktopApp/                      # 桌面应用模块
│   └── src/main/kotlin/
│       └── main.kt                  # 应用入口
└── build.gradle.kts                 # 构建配置
```

## 技术栈

- **Kotlin Multiplatform** - 跨平台框架
- **Compose Desktop** - 声明式UI框架
- **Material 3** - Material Design 3 设计规范
- **kotlinx-serialization** - JSON序列化
- **java.util.prefs** - 配置持久化
- **javax.crypto** - 密码加密

## 配置存储

- **密钥信息** - 存储在系统 Preferences 中（Windows 注册表）
- **应用设置** - 存储在系统 Preferences 中
- **加密密钥** - 自动生成并存储在 Preferences 中

## 许可证

MIT License
