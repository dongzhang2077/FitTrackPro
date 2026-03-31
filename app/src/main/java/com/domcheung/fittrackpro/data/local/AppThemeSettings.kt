package com.domcheung.fittrackpro.data.local

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromRaw(value: String?): AppThemeMode {
            return entries.firstOrNull { mode -> mode.name == value?.trim()?.uppercase() }
                ?: SYSTEM
        }
    }
}

data class AppThemeSettings(
    val mode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = false
)
