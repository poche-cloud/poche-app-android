package cloud.poche.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.SaveMemoUseCase
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class CaptureViewModel @Inject constructor(
    private val saveMemoUseCase: SaveMemoUseCase,
) : ViewModel() {

    private val _events = MutableSharedFlow<CaptureEvent>()
    val events = _events.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun saveMemo(content: String) {
        if (content.isBlank()) {
            viewModelScope.launch {
                _events.emit(CaptureEvent.ShowError("内容を入力してください"))
            }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val memo = Memo(
                    id = UUID.randomUUID().toString(),
                    content = content.trim(),
                    type = MemoType.TEXT,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
                saveMemoUseCase(memo)
                _events.emit(CaptureEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(CaptureEvent.ShowError("保存に失敗しました"))
            } finally {
                _isSaving.value = false
            }
        }
    }
}

internal sealed interface CaptureEvent {
    data object SaveSuccess : CaptureEvent
    data class ShowError(val message: String) : CaptureEvent
}
