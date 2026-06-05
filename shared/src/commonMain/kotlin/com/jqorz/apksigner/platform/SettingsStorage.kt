package com.jqorz.apksigner.platform

import com.jqorz.apksigner.model.AppSettings

interface SettingsStorage {
    fun load(): AppSettings
    fun save(settings: AppSettings)
}

expect fun createSettingsStorage(): SettingsStorage
