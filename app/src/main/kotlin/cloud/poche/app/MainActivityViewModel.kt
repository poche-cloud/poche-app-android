package cloud.poche.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.CheckForceUpdateUseCase
import cloud.poche.core.domain.usecase.ForceUpdateStatus
import cloud.poche.core.domain.usecase.GetUserDataUseCase
import cloud.poche.core.model.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
    private val checkForceUpdateUseCase: CheckForceUpdateUseCase,
) : ViewModel() {

    val darkThemeConfig: StateFlow<DarkThemeConfig> =
        getUserDataUseCase()
            .map { it.darkThemeConfig }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DarkThemeConfig.FOLLOW_SYSTEM,
            )

    val isOnboardingCompleted: StateFlow<Boolean?> =
        getUserDataUseCase()
            .map { it.isOnboardingCompleted }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    private val _forceUpdateStatus = MutableStateFlow(ForceUpdateStatus.UP_TO_DATE)
    val forceUpdateStatus: StateFlow<ForceUpdateStatus> = _forceUpdateStatus.asStateFlow()

    fun checkForceUpdate(currentVersion: String) {
        viewModelScope.launch {
            _forceUpdateStatus.value = checkForceUpdateUseCase(currentVersion)
        }
    }
}
