package cloud.poche.feature.settings.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.DeleteAllMemosUseCase
import cloud.poche.core.domain.usecase.ExportMemosUseCase
import cloud.poche.core.domain.usecase.GetStorageUsageUseCase
import cloud.poche.core.model.StorageUsage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class DataManagementViewModel @Inject constructor(
    private val deleteAllMemosUseCase: DeleteAllMemosUseCase,
    private val exportMemosUseCase: ExportMemosUseCase,
    private val getStorageUsageUseCase: GetStorageUsageUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DataManagementUiState>(DataManagementUiState.Loading)
    val uiState: StateFlow<DataManagementUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataManagementEvent>()
    val events = _events.asSharedFlow()

    init {
        loadStorageUsage()
    }

    private fun loadStorageUsage() {
        _uiState.value = DataManagementUiState.Success(
            storageUsage = getStorageUsageUseCase(),
        )
    }

    fun exportMemos() {
        viewModelScope.launch {
            try {
                val json = exportMemosUseCase()
                val file = File(context.cacheDir, "poche_export.json")
                file.writeText(json)
                _events.emit(DataManagementEvent.ExportSuccess(file))
            } catch (_: Exception) {
                _events.emit(DataManagementEvent.ShowError("エクスポートに失敗しました"))
            }
        }
    }

    fun deleteAllMemos() {
        viewModelScope.launch {
            try {
                deleteAllMemosUseCase()
                loadStorageUsage()
                _events.emit(DataManagementEvent.DeleteSuccess)
            } catch (_: Exception) {
                _events.emit(DataManagementEvent.ShowError("削除に失敗しました"))
            }
        }
    }
}

internal sealed interface DataManagementUiState {
    data object Loading : DataManagementUiState
    data class Success(val storageUsage: StorageUsage) : DataManagementUiState
}

internal sealed interface DataManagementEvent {
    data class ExportSuccess(val file: File) : DataManagementEvent
    data object DeleteSuccess : DataManagementEvent
    data class ShowError(val message: String) : DataManagementEvent
}
