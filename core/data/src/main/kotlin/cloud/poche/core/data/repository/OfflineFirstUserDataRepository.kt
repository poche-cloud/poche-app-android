package cloud.poche.core.data.repository

import cloud.poche.core.datastore.UserPreferencesDataSource
import cloud.poche.core.domain.repository.UserDataRepository
import cloud.poche.core.model.DarkThemeConfig
import cloud.poche.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineFirstUserDataRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : UserDataRepository {

    override val userData: Flow<UserData> = userPreferencesDataSource.userData

    override val locale: Flow<String> = userPreferencesDataSource.locale

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        userPreferencesDataSource.setOnboardingCompleted(completed)
    }

    override suspend fun setDarkThemeConfig(config: DarkThemeConfig) {
        userPreferencesDataSource.setDarkThemeConfig(config)
    }

    override suspend fun setLocale(locale: String) {
        userPreferencesDataSource.setLocale(locale)
    }
}
