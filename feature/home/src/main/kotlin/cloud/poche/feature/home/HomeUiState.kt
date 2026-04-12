package cloud.poche.feature.home

import cloud.poche.core.model.Memo
import cloud.poche.core.ui.UiText

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val memos: List<Memo>) : HomeUiState
    data class Error(val message: UiText) : HomeUiState
}
