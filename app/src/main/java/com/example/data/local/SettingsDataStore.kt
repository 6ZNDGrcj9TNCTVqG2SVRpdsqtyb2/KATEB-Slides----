package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.localization.AppLanguage
import com.example.engine.SlideAspectRatio
import com.example.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kateb_settings")

data class UserSettings(
    val language: AppLanguage = AppLanguage.AR,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val defaultAspectRatio: SlideAspectRatio = SlideAspectRatio.RATIO_16_9,
    val hasCompletedOnboarding: Boolean = false,
    val smartBatterySaverEnabled: Boolean = true,
    val biometricAppLockEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = true,
    val editorStabilizeScreen: Boolean = false,
    val editorSnapToGrid: Boolean = true,
    val editorShowGrid: Boolean = false,
    val editorShowSafeMargins: Boolean = false
)

class SettingsDataStore(private val context: Context) {
    private val keyLanguage = stringPreferencesKey("ui_language")
    private val keyTheme = stringPreferencesKey("theme_preference")
    private val keyAspectRatio = stringPreferencesKey("default_aspect_ratio")
    private val keyOnboardingDone = booleanPreferencesKey("onboarding_completed")
    private val keyBatterySaver = booleanPreferencesKey("smart_battery_saver_enabled")
    private val keyBiometricAppLock = booleanPreferencesKey("biometric_app_lock_enabled")
    private val keyCloudSync = booleanPreferencesKey("cloud_sync_enabled")
    private val keyEditorStabilize = booleanPreferencesKey("editor_stabilize_screen")
    private val keyEditorSnapGrid = booleanPreferencesKey("editor_snap_to_grid")
    private val keyEditorShowGrid = booleanPreferencesKey("editor_show_grid")
    private val keyEditorSafeMargins = booleanPreferencesKey("editor_show_safe_margins")

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            language = AppLanguage.fromCode(prefs[keyLanguage]),
            themePreference = runCatching {
                ThemePreference.valueOf(prefs[keyTheme] ?: ThemePreference.SYSTEM.name)
            }.getOrDefault(ThemePreference.SYSTEM),
            defaultAspectRatio = SlideAspectRatio.fromId(prefs[keyAspectRatio]),
            hasCompletedOnboarding = prefs[keyOnboardingDone] ?: false,
            smartBatterySaverEnabled = prefs[keyBatterySaver] ?: true,
            biometricAppLockEnabled = prefs[keyBiometricAppLock] ?: false,
            cloudSyncEnabled = prefs[keyCloudSync] ?: true,
            editorStabilizeScreen = prefs[keyEditorStabilize] ?: false,
            editorSnapToGrid = prefs[keyEditorSnapGrid] ?: true,
            editorShowGrid = prefs[keyEditorShowGrid] ?: false,
            editorShowSafeMargins = prefs[keyEditorSafeMargins] ?: false
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[keyLanguage] = language.code }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.dataStore.edit { it[keyTheme] = theme.name }
    }

    suspend fun setDefaultAspectRatio(ratio: SlideAspectRatio) {
        context.dataStore.edit { it[keyAspectRatio] = ratio.id }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[keyOnboardingDone] = completed }
    }

    suspend fun setSmartBatterySaverEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyBatterySaver] = enabled }
    }

    suspend fun setBiometricAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyBiometricAppLock] = enabled }
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyCloudSync] = enabled }
    }

    suspend fun setEditorStabilizeScreen(enabled: Boolean) {
        context.dataStore.edit { it[keyEditorStabilize] = enabled }
    }

    suspend fun setEditorSnapToGrid(enabled: Boolean) {
        context.dataStore.edit { it[keyEditorSnapGrid] = enabled }
    }

    suspend fun setEditorShowGrid(visible: Boolean) {
        context.dataStore.edit { it[keyEditorShowGrid] = visible }
    }

    suspend fun setEditorShowSafeMargins(visible: Boolean) {
        context.dataStore.edit { it[keyEditorSafeMargins] = visible }
    }
}
