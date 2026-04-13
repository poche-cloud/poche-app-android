package cloud.poche.feature.memo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cloud.poche.core.domain.usecase.GetMemoByIdUseCase
import cloud.poche.core.domain.usecase.UpdateMemoUseCase
import cloud.poche.core.ui.R
import cloud.poche.core.ui.UiText
import cloud.poche.feature.memo.navigation.MemoDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MemoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMemoByIdUseCase: GetMemoByIdUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<MemoDetailRoute>()

    private val _events = MutableSharedFlow<MemoDetailEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<MemoDetailUiState> =
        getMemoByIdUseCase(route.memoId)
            .map<_, MemoDetailUiState> { memo -> MemoDetailUiState.Success(memo = memo) }
            .catch { e ->
                emit(
                    MemoDetailUiState.Error(
                        message = e.message?.let { UiText.DynamicString(it) }
                            ?: UiText.StringResource(R.string.error_load_failed),
                    ),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MemoDetailUiState.Loading,
            )

    fun updateContent(content: String) {
        val currentState = uiState.value
        if (currentState !is MemoDetailUiState.Success) return
        if (content.isBlank()) {
            viewModelScope.launch {
                _events.emit(MemoDetailEvent.ShowError(UiText.StringResource(R.string.error_content_empty)))
            }
            return
        }

        viewModelScope.launch {
            try {
                val updated = currentState.memo.copy(
                    content = content.trim(),
                    updatedAt = System.currentTimeMillis(),
                )
                updateMemoUseCase(updated)
                _events.emit(MemoDetailEvent.Saved)
            } catch (e: Exception) {
                _events.emit(MemoDetailEvent.ShowError(UiText.StringResource(R.string.error_save_failed)))
            }
        }
    }
}

internal sealed interface MemoDetailEvent {
    data object Saved : MemoDetailEvent
    data class ShowError(val message: UiText) : MemoDetailEvent
}
