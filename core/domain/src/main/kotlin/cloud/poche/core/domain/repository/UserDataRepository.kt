package cloud.poche.core.domain.repository

import cloud.poche.core.model.DarkThemeConfig
import cloud.poche.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>
    val locale: Flow<String>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setDarkThemeConfig(config: DarkThemeConfig)
    suspend fun setLocale(locale: String)
    suspend fun setAiConsent(consented: Boolean)
    val defaultCaptureType: Flow<String?>
    suspend fun setDefaultCaptureType(type: String?)
}
