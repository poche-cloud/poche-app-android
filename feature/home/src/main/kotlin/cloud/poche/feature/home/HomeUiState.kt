package cloud.poche.feature.home

import cloud.poche.core.model.Memo

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val memos: List<Memo>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
