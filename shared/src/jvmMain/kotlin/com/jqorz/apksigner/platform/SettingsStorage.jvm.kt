package com.jqorz.apksigner.platform

import com.jqorz.apksigner.model.AppSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.prefs.Preferences

class JVMSettingsStorage : SettingsStorage {
    private val prefs = Preferences.userNodeForPackage(JVMSettingsStorage::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    override fun load(): AppSettings {
        val data = prefs.get(SETTINGS_KEY, null) ?: return AppSettings()
        return try {
            json.decodeFromString<AppSettings>(data)
        } catch (e: Exception) {
            AppSettings()
        }
    }

    override fun save(settings: AppSettings) {
        prefs.put(SETTINGS_KEY, json.encodeToString(settings))
    }

    companion object {
        private const val SETTINGS_KEY = "app_settings"
    }
}

actual fun createSettingsStorage(): SettingsStorage = JVMSettingsStorage()
