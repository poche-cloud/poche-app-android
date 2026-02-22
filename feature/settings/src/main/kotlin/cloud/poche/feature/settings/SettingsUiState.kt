package cloud.poche.feature.settings

import cloud.poche.core.model.DarkThemeConfig

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val userId: String?,
        val isSignedIn: Boolean,
        val darkThemeConfig: DarkThemeConfig,
        val appVersion: String,
        val isDebugBuild: Boolean = false,
    ) : SettingsUiState
}
