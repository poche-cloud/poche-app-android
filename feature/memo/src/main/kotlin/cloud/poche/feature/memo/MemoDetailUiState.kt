package cloud.poche.feature.memo

import cloud.poche.core.model.Memo

internal sealed interface MemoDetailUiState {
    data object Loading : MemoDetailUiState
    data class Success(val memo: Memo) : MemoDetailUiState
    data class Error(val message: String) : MemoDetailUiState
}
