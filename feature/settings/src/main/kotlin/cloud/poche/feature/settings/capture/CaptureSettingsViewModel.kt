package cloud.poche.feature.settings.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.GetDefaultCaptureTypeUseCase
import cloud.poche.core.domain.usecase.SetDefaultCaptureTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CaptureSettingsViewModel @Inject constructor(
    getDefaultCaptureTypeUseCase: GetDefaultCaptureTypeUseCase,
    private val setDefaultCaptureTypeUseCase: SetDefaultCaptureTypeUseCase,
) : ViewModel() {

    val uiState: StateFlow<CaptureSettingsUiState> =
        getDefaultCaptureTypeUseCase()
            .map { type -> CaptureSettingsUiState.Success(defaultCaptureType = type) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CaptureSettingsUiState.Loading,
            )

    fun setDefaultCaptureType(type: String?) {
        viewModelScope.launch {
            setDefaultCaptureTypeUseCase(type)
        }
    }
}

internal sealed interface CaptureSettingsUiState {
    data object Loading : CaptureSettingsUiState
    data class Success(val defaultCaptureType: String?) : CaptureSettingsUiState
}
