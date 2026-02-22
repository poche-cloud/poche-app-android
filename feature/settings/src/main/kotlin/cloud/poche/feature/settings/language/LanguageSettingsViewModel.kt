package cloud.poche.feature.settings.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.GetLocaleUseCase
import cloud.poche.core.domain.usecase.SetLocaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LanguageSettingsViewModel @Inject constructor(
    getLocaleUseCase: GetLocaleUseCase,
    private val setLocaleUseCase: SetLocaleUseCase,
) : ViewModel() {

    val uiState: StateFlow<LanguageSettingsUiState> =
        getLocaleUseCase()
            .map { locale ->
                LanguageSettingsUiState.Success(
                    currentLocale = locale.ifEmpty { resolveSystemLocale() },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LanguageSettingsUiState.Loading,
            )

    fun setLocale(locale: String) {
        viewModelScope.launch {
            setLocaleUseCase(locale)
            val localeList = LocaleListCompat.forLanguageTags(locale)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    private fun resolveSystemLocale(): String {
        val systemLocale = AppCompatDelegate.getApplicationLocales()
        if (!systemLocale.isEmpty) {
            return systemLocale.get(0)?.language ?: "ja"
        }
        return java.util.Locale.getDefault().language
    }
}

internal sealed interface LanguageSettingsUiState {
    data object Loading : LanguageSettingsUiState
    data class Success(val currentLocale: String) : LanguageSettingsUiState
}
