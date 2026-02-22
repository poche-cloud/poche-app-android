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

class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
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

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
    }
}

private fun String.toDarkThemeConfig(): DarkThemeConfig =
    runCatching { DarkThemeConfig.valueOf(this) }
        .getOrDefault(DarkThemeConfig.FOLLOW_SYSTEM)
