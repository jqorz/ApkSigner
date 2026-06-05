package com.jqorz.apksigner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jqorz.apksigner.model.KeyStoreInfo
import com.jqorz.apksigner.model.SignScheme
import com.jqorz.apksigner.model.SigningState
import com.jqorz.apksigner.model.StoreType
import com.jqorz.apksigner.ui.SettingsDialog
import com.jqorz.apksigner.viewmodel.SignViewModel

// 自定义颜色
private val AppBlue = Color(0xFF2196F3)
private val AppBlueLight = Color(0xFFBBDEFB)
private val AppBlueDark = Color(0xFF1565C0)
private val AppGreen = Color(0xFF4CAF50)
private val AppGreenLight = Color(0xFFC8E6C9)
private val AppRed = Color(0xFFE53935)
private val AppRedLight = Color(0xFFFFCDD2)
private val AppSurface = Color(0xFFF5F5F5)
private val AppCardBg = Color(0xFFFFFFFF)

private fun appColorScheme() = lightColorScheme(
    primary = AppBlueDark,
    onPrimary = Color.White,
    primaryContainer = AppBlueLight,
    onPrimaryContainer = AppBlueDark,
    secondary = AppGreen,
    onSecondary = Color.White,
    secondaryContainer = AppGreenLight,
    onSecondaryContainer = Color(0xFF1B5E20),
    error = AppRed,
    onError = Color.White,
    errorContainer = AppRedLight,
    onErrorContainer = Color(0xFFB71C1C),
    background = AppSurface,
    onBackground = Color(0xFF212121),
    surface = AppCardBg,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF616161)
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun App() {
    val viewModel: SignViewModel = viewModel { SignViewModel() }
    val showSettings by viewModel.showSettings.collectAsState()
    val settings by viewModel.settings.collectAsState()

    MaterialTheme(
        colorScheme = appColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部标题栏
                TopAppBar(
                    title = {
                        Text(
                            "APK签名工具",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.showSettingsDialog() }) {
                            Text("⚙", fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                // 主内容区域
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 左侧：已保存密钥列表
                    SavedKeysPanel(
                        viewModel = viewModel,
                        modifier = Modifier.weight(0.3f)
                    )

                    // 右侧：签名配置
                    SignConfigPanel(
                        viewModel = viewModel,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }

            // 设置对话框
            if (showSettings) {
                SettingsDialog(
                    apksignerPath = settings.apksignerPath,
                    androidSdkPath = settings.androidSdkPath,
                    onDismiss = { viewModel.hideSettingsDialog() },
                    onSelectApkSigner = { viewModel.selectApkSignerFile() },
                    onSelectSdkPath = { viewModel.selectAndroidSdkPath() },
                    onAutoDetect = { viewModel.autoDetectApkSigner() },
                    onSave = { apksignerPath, sdkPath ->
                        viewModel.saveSettings(apksignerPath, sdkPath)
                    }
                )
            }
        }
    }
}

@Composable
private fun SavedKeysPanel(
    viewModel: SignViewModel,
    modifier: Modifier = Modifier
) {
    val savedKeys by viewModel.savedKeys.collectAsState()
    val selectedKey by viewModel.selectedKey.collectAsState()
    val showKeyForm by viewModel.showKeyForm.collectAsState()

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题和添加按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "已保存的密钥",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = { viewModel.showNewKeyForm() }
                ) {
                    Text("+ 添加")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showKeyForm) {
                // 密钥表单
                KeyStoreForm(viewModel = viewModel)
            } else if (savedKeys.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无保存的密钥\n点击上方按钮添加",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }
            } else {
                // 密钥列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedKeys) { key ->
                        KeyStoreItem(
                            key = key,
                            isSelected = selectedKey?.id == key.id,
                            onSelect = { viewModel.selectSavedKey(key) },
                            onEdit = { viewModel.editKey(key) },
                            onDelete = { viewModel.deleteKey(key.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyStoreItem(
    key: KeyStoreInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    key.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    key.keyAlias,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    key.filePath.substringAfterLast("/").substringAfterLast("\\"),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Text("✏", fontSize = 16.sp)
                }
                IconButton(onClick = onDelete) {
                    Text("🗑", fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyStoreForm(viewModel: SignViewModel) {
    val name by viewModel.formName.collectAsState()
    val filePath by viewModel.formFilePath.collectAsState()
    val storePassword by viewModel.formStorePassword.collectAsState()
    val keyAlias by viewModel.formKeyAlias.collectAsState()
    val keyPassword by viewModel.formKeyPassword.collectAsState()
    val storeType by viewModel.formStoreType.collectAsState()
    val editingKeyId by viewModel.editingKeyId.collectAsState()
    val availableAliases by viewModel.availableAliases.collectAsState()
    val isLoadingAliases by viewModel.isLoadingAliases.collectAsState()
    val isEditing = editingKeyId != null

    var aliasExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (isEditing) "编辑密钥" else "添加新密钥",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        // 名称
        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.updateFormName(it) },
            label = { Text("密钥名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 文件路径选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = filePath,
                onValueChange = {},
                label = { Text("密钥库文件") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true
            )
            FilledTonalButton(
                onClick = { viewModel.selectKeyStore() },
                modifier = Modifier.height(56.dp)
            ) {
                Text("浏览")
            }
        }

        // 密钥库类型
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = storeType == StoreType.JKS,
                onClick = { viewModel.updateFormStoreType(StoreType.JKS) },
                label = { Text("JKS") }
            )
            FilterChip(
                selected = storeType == StoreType.PKCS12,
                onClick = { viewModel.updateFormStoreType(StoreType.PKCS12) },
                label = { Text("PKCS12") }
            )
        }

        // 密钥库密码
        OutlinedTextField(
            value = storePassword,
            onValueChange = { viewModel.updateFormStorePassword(it) },
            label = { Text("密钥库密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        // 密钥别名 - 下拉框
        ExposedDropdownMenuBox(
            expanded = aliasExpanded && availableAliases.isNotEmpty(),
            onExpandedChange = {
                if (availableAliases.isNotEmpty()) {
                    aliasExpanded = it
                }
            }
        ) {
            OutlinedTextField(
                value = keyAlias,
                onValueChange = { viewModel.updateFormKeyAlias(it) },
                label = {
                    Text(
                        if (isLoadingAliases) "正在加载别名..." else "密钥别名"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                singleLine = true,
                trailingIcon = {
                    if (isLoadingAliases) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (availableAliases.isNotEmpty()) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = aliasExpanded)
                    }
                },
                supportingText = if (availableAliases.isEmpty() && filePath.isNotBlank() && storePassword.isNotBlank() && !isLoadingAliases) {
                    { Text("未找到别名，请手动输入") }
                } else null
            )

            if (availableAliases.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = aliasExpanded,
                    onDismissRequest = { aliasExpanded = false }
                ) {
                    availableAliases.forEach { alias ->
                        DropdownMenuItem(
                            text = { Text(alias) },
                            onClick = {
                                viewModel.updateFormKeyAlias(alias)
                                aliasExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 密钥密码
        OutlinedTextField(
            value = keyPassword,
            onValueChange = { viewModel.updateFormKeyPassword(it) },
            label = { Text("密钥密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        // 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.hideKeyForm() },
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
            Button(
                onClick = { viewModel.saveCurrentKey() },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && filePath.isNotBlank() &&
                        storePassword.isNotBlank() && keyAlias.isNotBlank() && keyPassword.isNotBlank()
            ) {
                Text(if (isEditing) "更新" else "保存")
            }
        }
    }
}

@Composable
private fun SignConfigPanel(
    viewModel: SignViewModel,
    modifier: Modifier = Modifier
) {
    val apkPath by viewModel.apkPath.collectAsState()
    val selectedKey by viewModel.selectedKey.collectAsState()
    val signSchemes by viewModel.signSchemes.collectAsState()
    val signingState by viewModel.signingState.collectAsState()
    val apksignerPath by viewModel.apksignerPath.collectAsState()

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 标题行：签名配置 + apksigner状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "签名配置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                ApkSignerStatusChip(
                    apksignerPath = apksignerPath,
                    onRefresh = { viewModel.refreshApkSigner() }
                )
            }

            // APK文件选择
            ApkFileSection(
                apkPath = apkPath,
                onSelect = { viewModel.selectApk() }
            )

            // 选中的密钥信息
            SelectedKeySection(selectedKey = selectedKey)

            // 签名方案选择
            SignSchemeSection(
                signSchemes = signSchemes,
                onToggle = { viewModel.toggleSignScheme(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 签名按钮和状态
            SignActionSection(
                signingState = signingState,
                canSign = apkPath.isNotBlank() && selectedKey != null && !apksignerPath.isNullOrBlank(),
                onSign = { viewModel.startSigning() },
                onReset = { viewModel.resetSigningState() }
            )
        }
    }
}

@Composable
private fun ApkFileSection(
    apkPath: String,
    onSelect: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "APK文件",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = apkPath,
                onValueChange = {},
                label = { Text("选择要签名的APK文件") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true
            )
            FilledTonalButton(
                onClick = onSelect,
                modifier = Modifier.height(56.dp)
            ) {
                Text("浏览")
            }
        }
    }
}

@Composable
private fun SelectedKeySection(selectedKey: KeyStoreInfo?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "当前密钥",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        if (selectedKey != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        selectedKey.name,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "别名: ${selectedKey.keyAlias}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "类型: ${selectedKey.storeType}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "请从左侧列表选择一个密钥",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun SignSchemeSection(
    signSchemes: Set<SignScheme>,
    onToggle: (SignScheme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "签名方案",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SignScheme.entries.forEach { scheme ->
                FilterChip(
                    selected = signSchemes.contains(scheme),
                    onClick = { onToggle(scheme) },
                    label = { Text(scheme.displayName) }
                )
            }
        }
    }
}

@Composable
private fun ApkSignerStatusChip(
    apksignerPath: String?,
    onRefresh: () -> Unit
) {
    val containerColor = if (apksignerPath != null) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (apksignerPath != null) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        onClick = onRefresh,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (apksignerPath != null) "✓ apksigner" else "✗ 未找到",
                color = contentColor,
                fontSize = 12.sp
            )
            Text(
                text = "刷新",
                color = contentColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SignActionSection(
    signingState: SigningState,
    canSign: Boolean,
    onSign: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 状态显示
        when (signingState) {
            is SigningState.Idle -> { /* 不显示 */ }
            is SigningState.Signing -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppBlueLight
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = AppBlueDark
                        )
                        Text(
                            signingState.message,
                            color = AppBlueDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            is SigningState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppGreenLight
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    SelectionContainer(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                "✓ 签名成功!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "输出: ${signingState.outputPath}",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
            is SigningState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppRedLight
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    SelectionContainer(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                "✗ 签名失败",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                signingState.message,
                                fontSize = 12.sp,
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }
                }
            }
        }

        // 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (signingState is SigningState.Success || signingState is SigningState.Error) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }
            }
            Button(
                onClick = onSign,
                modifier = Modifier.weight(1f),
                enabled = canSign && signingState is SigningState.Idle
            ) {
                Text(
                    if (signingState is SigningState.Signing) "签名中..." else "开始签名",
                    fontSize = 16.sp
                )
            }
        }
    }
}
