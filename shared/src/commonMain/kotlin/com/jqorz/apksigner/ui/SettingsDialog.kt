package com.jqorz.apksigner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SettingsDialog(
    apksignerPath: String?,
    androidSdkPath: String?,
    detectMessage: String?,
    onDismiss: () -> Unit,
    onSelectApkSigner: () -> Unit,
    onSelectSdkPath: () -> Unit,
    onAutoDetect: () -> Unit,
    onSave: (apksignerPath: String?, androidSdkPath: String?) -> Unit
) {
    var editedApkSignerPath by remember { mutableStateOf(apksignerPath ?: "") }
    var editedSdkPath by remember { mutableStateOf(androidSdkPath ?: "") }

    // 同步ViewModel状态变化（自动检测、浏览选择后）
    LaunchedEffect(apksignerPath) {
        editedApkSignerPath = apksignerPath ?: ""
    }
    LaunchedEffect(androidSdkPath) {
        editedSdkPath = androidSdkPath ?: ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题
                Text(
                    "设置",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                // Android SDK路径
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Android SDK路径",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        "设置Android SDK根目录，用于自动查找apksigner",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editedSdkPath,
                            onValueChange = { editedSdkPath = it },
                            placeholder = { Text("例如: C:\\Users\\xxx\\AppData\\Local\\Android\\Sdk") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        FilledTonalButton(
                            onClick = onSelectSdkPath,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("浏览")
                        }
                    }
                }

                // apksigner路径
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "apksigner路径",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        "手动指定apksigner可执行文件路径（优先级高于SDK路径）",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editedApkSignerPath,
                            onValueChange = { editedApkSignerPath = it },
                            placeholder = { Text("留空则自动从SDK路径查找") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        FilledTonalButton(
                            onClick = onSelectApkSigner,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("浏览")
                        }
                    }
                }

                // 自动检测按钮
                OutlinedButton(
                    onClick = onAutoDetect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("自动检测")
                }

                // 检测结果提示
                if (detectMessage != null) {
                    val isSuccess = detectMessage.startsWith("✓")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = detectMessage,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            color = if (isSuccess) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }

                HorizontalDivider()

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            onSave(
                                editedApkSignerPath.takeIf { it.isNotBlank() },
                                editedSdkPath.takeIf { it.isNotBlank() }
                            )
                        }
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}
