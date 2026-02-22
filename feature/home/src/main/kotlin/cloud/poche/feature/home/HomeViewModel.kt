package cloud.poche.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.common.result.Result
import cloud.poche.core.common.result.asResult
import cloud.poche.core.domain.usecase.GetMemosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getMemosUseCase: GetMemosUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        getMemosUseCase()
            .asResult()
            .map { result ->
                when (result) {
                    is Result.Loading -> HomeUiState.Loading
                    is Result.Success -> HomeUiState.Success(memos = result.data)
                    is Result.Error -> HomeUiState.Error(
                        message = result.exception.message ?: "Unknown error",
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState.Loading,
            )
}
