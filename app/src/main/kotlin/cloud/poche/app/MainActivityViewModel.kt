package cloud.poche.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.GetUserDataUseCase
import cloud.poche.core.model.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
) : ViewModel() {

    val darkThemeConfig: StateFlow<DarkThemeConfig> =
        getUserDataUseCase()
            .map { it.darkThemeConfig }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DarkThemeConfig.FOLLOW_SYSTEM,
            )
}
