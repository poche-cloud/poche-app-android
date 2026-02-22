package cloud.poche.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.common.result.Result
import cloud.poche.core.common.result.asResult
import cloud.poche.core.domain.usecase.DeleteMemoUseCase
import cloud.poche.core.domain.usecase.GetMemosUseCase
import cloud.poche.core.domain.usecase.SaveMemoUseCase
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMemosUseCase: GetMemosUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val saveMemoUseCase: SaveMemoUseCase,
) : ViewModel() {

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> =
        retryTrigger
            .flatMapLatest {
                getMemosUseCase()
                    .asResult()
                    .map { result ->
                        when (result) {
                            is Result.Loading -> HomeUiState.Loading
                            is Result.Success -> HomeUiState.Success(memos = result.data)
                            is Result.Error -> HomeUiState.Error(
                                message = result.exception.message ?: "不明なエラー",
                            )
                        }
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState.Loading,
            )

    fun retry() {
        retryTrigger.value++
    }

    fun deleteMemo(id: String) {
        viewModelScope.launch {
            try {
                deleteMemoUseCase(id)
            } catch (e: Exception) {
                _events.emit(HomeEvent.ShowError("削除に失敗しました"))
            }
        }
    }

    fun quickCapture(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val memo = Memo(
                id = UUID.randomUUID().toString(),
                content = content.trim(),
                type = MemoType.TEXT,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            try {
                saveMemoUseCase(memo)
                _events.emit(HomeEvent.CaptureSuccess)
            } catch (e: Exception) {
                _events.emit(HomeEvent.ShowError("保存に失敗しました"))
            }
        }
    }
}

sealed interface HomeEvent {
    data object CaptureSuccess : HomeEvent
    data class ShowError(val message: String) : HomeEvent
}
