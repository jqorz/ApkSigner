package com.jqorz.apksigner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jqorz.apksigner.model.AppSettings
import com.jqorz.apksigner.model.KeyStoreInfo
import com.jqorz.apksigner.model.SignConfig
import com.jqorz.apksigner.model.SignScheme
import com.jqorz.apksigner.model.SigningState
import com.jqorz.apksigner.model.StoreType
import com.jqorz.apksigner.platform.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SignViewModel : ViewModel() {
    private val filePicker = createFilePicker()
    private val keyStoreStorage = createKeyStoreStorage()
    private val apkSignerTool = createApkSignerTool()
    private val settingsStorage = createSettingsStorage()
    private val keyStoreReader = createKeyStoreReader()

    // 设置
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // APK路径
    private val _apkPath = MutableStateFlow("")
    val apkPath: StateFlow<String> = _apkPath.asStateFlow()

    // 选中的密钥
    private val _selectedKey = MutableStateFlow<KeyStoreInfo?>(null)
    val selectedKey: StateFlow<KeyStoreInfo?> = _selectedKey.asStateFlow()

    // 已保存的密钥列表
    private val _savedKeys = MutableStateFlow<List<KeyStoreInfo>>(emptyList())
    val savedKeys: StateFlow<List<KeyStoreInfo>> = _savedKeys.asStateFlow()

    // 签名方案
    private val _signSchemes = MutableStateFlow(setOf(SignScheme.V1, SignScheme.V2, SignScheme.V3))
    val signSchemes: StateFlow<Set<SignScheme>> = _signSchemes.asStateFlow()

    // 签名状态
    private val _signingState = MutableStateFlow<SigningState>(SigningState.Idle)
    val signingState: StateFlow<SigningState> = _signingState.asStateFlow()

    // apksigner路径
    private val _apksignerPath = MutableStateFlow<String?>(null)
    val apksignerPath: StateFlow<String?> = _apksignerPath.asStateFlow()

    // 密钥表单状态
    private val _showKeyForm = MutableStateFlow(false)
    val showKeyForm: StateFlow<Boolean> = _showKeyForm.asStateFlow()

    // 正在编辑的密钥ID（null表示新建）
    private val _editingKeyId = MutableStateFlow<String?>(null)
    val editingKeyId: StateFlow<String?> = _editingKeyId.asStateFlow()

    // 可用别名列表
    private val _availableAliases = MutableStateFlow<List<String>>(emptyList())
    val availableAliases: StateFlow<List<String>> = _availableAliases.asStateFlow()

    // 是否正在加载别名
    private val _isLoadingAliases = MutableStateFlow(false)
    val isLoadingAliases: StateFlow<Boolean> = _isLoadingAliases.asStateFlow()

    // 表单字段
    private val _formName = MutableStateFlow("")
    val formName: StateFlow<String> = _formName.asStateFlow()

    private val _formFilePath = MutableStateFlow("")
    val formFilePath: StateFlow<String> = _formFilePath.asStateFlow()

    private val _formStorePassword = MutableStateFlow("")
    val formStorePassword: StateFlow<String> = _formStorePassword.asStateFlow()

    private val _formKeyAlias = MutableStateFlow("")
    val formKeyAlias: StateFlow<String> = _formKeyAlias.asStateFlow()

    private val _formKeyPassword = MutableStateFlow("")
    val formKeyPassword: StateFlow<String> = _formKeyPassword.asStateFlow()

    private val _formStoreType = MutableStateFlow(StoreType.JKS)
    val formStoreType: StateFlow<StoreType> = _formStoreType.asStateFlow()

    init {
        loadSettings()
        loadSavedKeys()
        detectApkSigner()
    }

    private fun loadSettings() {
        _settings.value = settingsStorage.load()
    }

    private fun loadSavedKeys() {
        _savedKeys.value = keyStoreStorage.getAllKeyStores()
    }

    private fun detectApkSigner() {
        val currentSettings = _settings.value
        // 优先使用手动配置的路径
        if (!currentSettings.apksignerPath.isNullOrBlank()) {
            _apksignerPath.value = currentSettings.apksignerPath
            return
        }
        // 否则自动检测
        _apksignerPath.value = apkSignerTool.detectApkSigner()
    }

    // 设置相关
    fun showSettingsDialog() {
        _showSettings.value = true
    }

    fun hideSettingsDialog() {
        _showSettings.value = false
    }

    fun selectApkSignerFile() {
        val path = filePicker.selectApkSignerFile()
        if (path != null) {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(apksignerPath = path)
        }
    }

    fun selectAndroidSdkPath() {
        val path = filePicker.selectDirectory()
        if (path != null) {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(androidSdkPath = path)
        }
    }

    fun saveSettings(apksignerPath: String?, androidSdkPath: String?) {
        val currentSettings = _settings.value
        val newSettings = currentSettings.copy(
            apksignerPath = apksignerPath,
            androidSdkPath = androidSdkPath
        )
        settingsStorage.save(newSettings)
        _settings.value = newSettings
        _showSettings.value = false
        // 重新检测apksigner
        detectApkSigner()
    }

    fun autoDetectApkSigner() {
        _apksignerPath.value = apkSignerTool.detectApkSigner()
        // 清空手动配置，使用自动检测结果
        val currentSettings = _settings.value
        _settings.value = currentSettings.copy(apksignerPath = null)
        settingsStorage.save(_settings.value)
    }

    fun selectApk() {
        val path = filePicker.selectApkFile()
        if (path != null) {
            _apkPath.value = path
        }
    }

    fun selectKeyStore() {
        val path = filePicker.selectKeyStoreFile()
        if (path != null) {
            _formFilePath.value = path
            // 根据扩展名判断类型
            _formStoreType.value = when {
                path.endsWith(".p12") || path.endsWith(".pfx") -> StoreType.PKCS12
                else -> StoreType.JKS
            }
            // 尝试加载别名
            loadAliasesIfReady()
        }
    }

    fun showNewKeyForm() {
        resetForm()
        _editingKeyId.value = null
        _showKeyForm.value = true
    }

    fun editKey(info: KeyStoreInfo) {
        _editingKeyId.value = info.id
        _formName.value = info.name
        _formFilePath.value = info.filePath
        _formStorePassword.value = keyStoreStorage.decryptPassword(info.storePasswordEncrypted)
        _formKeyAlias.value = info.keyAlias
        _formKeyPassword.value = keyStoreStorage.decryptPassword(info.keyPasswordEncrypted)
        _formStoreType.value = info.storeType
        _showKeyForm.value = true
    }

    fun hideKeyForm() {
        _showKeyForm.value = false
        _editingKeyId.value = null
    }

    fun updateFormName(value: String) {
        _formName.value = value
    }

    fun updateFormStorePassword(value: String) {
        _formStorePassword.value = value
        // 当密码和文件路径都有值时，尝试加载别名
        loadAliasesIfReady()
    }

    fun updateFormKeyAlias(value: String) {
        _formKeyAlias.value = value
    }

    private fun loadAliasesIfReady() {
        val filePath = _formFilePath.value.trim()
        val storePassword = _formStorePassword.value
        val storeType = _formStoreType.value.name

        if (filePath.isNotBlank() && storePassword.isNotBlank()) {
            viewModelScope.launch {
                _isLoadingAliases.value = true
                val aliases = withContext(Dispatchers.IO) {
                    keyStoreReader.getAliases(filePath, storePassword, storeType)
                }
                _availableAliases.value = aliases
                _isLoadingAliases.value = false
                // 如果只有一个别名，自动选择
                if (aliases.size == 1) {
                    _formKeyAlias.value = aliases[0]
                }
            }
        } else {
            _availableAliases.value = emptyList()
        }
    }

    fun refreshAliases() {
        loadAliasesIfReady()
    }

    fun updateFormKeyPassword(value: String) {
        _formKeyPassword.value = value
    }

    fun updateFormStoreType(value: StoreType) {
        _formStoreType.value = value
    }

    fun saveCurrentKey() {
        val name = _formName.value.trim()
        val filePath = _formFilePath.value.trim()
        val storePassword = _formStorePassword.value
        val keyAlias = _formKeyAlias.value.trim()
        val keyPassword = _formKeyPassword.value

        if (name.isBlank() || filePath.isBlank() || storePassword.isBlank() || keyAlias.isBlank() || keyPassword.isBlank()) {
            return
        }

        val existingId = _editingKeyId.value
        val info = KeyStoreInfo(
            id = existingId ?: UUID.randomUUID().toString(),
            name = name,
            filePath = filePath,
            storePasswordEncrypted = keyStoreStorage.encryptPassword(storePassword),
            keyAlias = keyAlias,
            keyPasswordEncrypted = keyStoreStorage.encryptPassword(keyPassword),
            storeType = _formStoreType.value,
            createdAt = System.currentTimeMillis()
        )

        keyStoreStorage.saveKeyStore(info)
        loadSavedKeys()
        _selectedKey.value = info
        _showKeyForm.value = false
        _editingKeyId.value = null
        resetForm()
    }

    fun selectSavedKey(info: KeyStoreInfo) {
        _selectedKey.value = info
    }

    fun deleteKey(id: String) {
        keyStoreStorage.deleteKeyStore(id)
        loadSavedKeys()
        if (_selectedKey.value?.id == id) {
            _selectedKey.value = null
        }
    }

    fun toggleSignScheme(scheme: SignScheme) {
        val current = _signSchemes.value.toMutableSet()
        if (current.contains(scheme)) {
            current.remove(scheme)
        } else {
            current.add(scheme)
        }
        _signSchemes.value = current
    }

    fun startSigning() {
        val apk = _apkPath.value
        val key = _selectedKey.value
        val schemes = _signSchemes.value
        val apksigner = _apksignerPath.value

        if (apk.isBlank()) {
            _signingState.value = SigningState.Error("请选择APK文件")
            return
        }
        if (key == null) {
            _signingState.value = SigningState.Error("请选择密钥")
            return
        }
        if (apksigner.isNullOrBlank()) {
            _signingState.value = SigningState.Error("未找到apksigner工具，请确保已安装Android SDK")
            return
        }
        if (schemes.isEmpty()) {
            _signingState.value = SigningState.Error("请至少选择一种签名方案")
            return
        }

        val config = SignConfig(
            apkPath = apk,
            keyStoreInfo = key,
            signSchemes = schemes
        )

        viewModelScope.launch {
            _signingState.value = SigningState.Signing("正在签名...")

            val result = withContext(Dispatchers.IO) {
                apkSignerTool.sign(config, apksigner) { message ->
                    _signingState.value = SigningState.Signing(message)
                }
            }

            _signingState.value = if (result.success) {
                SigningState.Success(result.outputPath ?: "")
            } else {
                SigningState.Error(result.error ?: "未知错误")
            }
        }
    }

    fun resetSigningState() {
        _signingState.value = SigningState.Idle
    }

    fun refreshApkSigner() {
        detectApkSigner()
    }

    private fun resetForm() {
        _formName.value = ""
        _formFilePath.value = ""
        _formStorePassword.value = ""
        _formKeyAlias.value = ""
        _formKeyPassword.value = ""
        _formStoreType.value = StoreType.JKS
    }
}
