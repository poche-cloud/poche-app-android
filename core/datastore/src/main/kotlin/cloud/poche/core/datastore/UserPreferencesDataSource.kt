package cloud.poche.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cloud.poche.core.model.DarkThemeConfig
import cloud.poche.core.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(private val dataStore: DataStore<Preferences>) {
    val userData: Flow<UserData> = dataStore.data.map { prefs ->
        UserData(
            isOnboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: false,
            userId = null,
            darkThemeConfig = prefs[DARK_THEME_CONFIG]
                ?.toDarkThemeConfig()
                ?: DarkThemeConfig.FOLLOW_SYSTEM,
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDarkThemeConfig(config: DarkThemeConfig) {
        dataStore.edit { prefs ->
            prefs[DARK_THEME_CONFIG] = config.name
        }
    }

    val locale: Flow<String> = dataStore.data.map { prefs ->
        prefs[LOCALE] ?: ""
    }

    suspend fun setLocale(locale: String) {
        dataStore.edit { prefs ->
            prefs[LOCALE] = locale
        }
    }

    suspend fun setFcmToken(token: String) {
        dataStore.edit { prefs ->
            prefs[FCM_TOKEN] = token
        }
    }

    val defaultCaptureType: Flow<String?> = dataStore.data.map { prefs ->
        prefs[DEFAULT_CAPTURE_TYPE]
    }

    suspend fun setDefaultCaptureType(type: String?) {
        dataStore.edit { prefs ->
            if (type == null) {
                prefs.remove(DEFAULT_CAPTURE_TYPE)
            } else {
                prefs[DEFAULT_CAPTURE_TYPE] = type
            }
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
        val LOCALE = stringPreferencesKey("locale")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val DEFAULT_CAPTURE_TYPE = stringPreferencesKey("default_capture_type")
    }
}

private fun String.toDarkThemeConfig(): DarkThemeConfig = runCatching { DarkThemeConfig.valueOf(this) }
    .getOrDefault(DarkThemeConfig.FOLLOW_SYSTEM)
