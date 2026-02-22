package cloud.poche.feature.settings.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.GetUserDataUseCase
import cloud.poche.core.domain.usecase.SetDarkThemeConfigUseCase
import cloud.poche.core.model.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
    private val setDarkThemeConfigUseCase: SetDarkThemeConfigUseCase,
) : ViewModel() {

    val uiState: StateFlow<ThemeSettingsUiState> =
        getUserDataUseCase()
            .map { userData ->
                ThemeSettingsUiState.Success(
                    darkThemeConfig = userData.darkThemeConfig,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ThemeSettingsUiState.Loading,
            )

    fun setTheme(config: DarkThemeConfig) {
        viewModelScope.launch {
            setDarkThemeConfigUseCase(config)
        }
    }
}

sealed interface ThemeSettingsUiState {
    data object Loading : ThemeSettingsUiState
    data class Success(val darkThemeConfig: DarkThemeConfig) : ThemeSettingsUiState
}
