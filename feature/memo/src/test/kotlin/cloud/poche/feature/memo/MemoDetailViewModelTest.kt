package cloud.poche.feature.memo

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.domain.usecase.GetMemoByIdUseCase
import cloud.poche.core.domain.usecase.UpdateMemoUseCase
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.R
import cloud.poche.core.ui.UiText
import cloud.poche.feature.memo.navigation.MemoDetailRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@OptIn(ExperimentalCoroutinesApi::class)
class MemoDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RecordingMemoRepository
    private lateinit var viewModel: MemoDetailViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = RecordingMemoRepository()
        repository.emitMemos(listOf(sampleMemo("memo-1", "original")))
        viewModel = createViewModel("memo-1")
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState starts with loading state`() {
        assertEquals(MemoDetailUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState maps memo id to success`() = runTest {
        viewModel.uiState.test {
            val success = awaitSuccess()

            assertEquals("memo-1", success.memo.id)
            assertEquals("original", success.memo.content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState maps repository failure to error`() = runTest {
        repository.loadError = IllegalStateException("memo unavailable")
        viewModel = createViewModel("memo-1")

        viewModel.uiState.test {
            val error = awaitErrorState()

            assertEquals("memo unavailable", (error.message as UiText.DynamicString).value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateContent trims content and emits saved event`() = runTest {
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.events.test {
                viewModel.updateContent("  updated content  ")
                testDispatcher.scheduler.advanceUntilIdle()

                assertEquals(MemoDetailEvent.Saved, awaitItem())
                assertEquals("updated content", repository.updatedMemos.single().content)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateContent emits validation error for blank content`() = runTest {
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.events.test {
                viewModel.updateContent("   ")
                testDispatcher.scheduler.advanceUntilIdle()

                assertStringResource(R.string.error_content_empty, awaitErrorEvent().message)
                assertEquals(emptyList<Memo>(), repository.updatedMemos)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateContent emits save error when update fails`() = runTest {
        repository.updateError = IllegalStateException("save failed")

        viewModel.uiState.test {
            awaitSuccess()

            viewModel.events.test {
                viewModel.updateContent("updated")
                testDispatcher.scheduler.advanceUntilIdle()

                assertStringResource(R.string.error_save_failed, awaitErrorEvent().message)
                assertEquals(emptyList<Memo>(), repository.updatedMemos)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(memoId: String) = MemoDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(MemoDetailRoute.MEMO_ID_ARG to memoId)),
        getMemoByIdUseCase = GetMemoByIdUseCase(repository),
        updateMemoUseCase = UpdateMemoUseCase(repository),
    )

    private suspend fun ReceiveTurbine<MemoDetailUiState>.awaitSuccess(): MemoDetailUiState.Success {
        repeat(4) {
            val item = awaitItem()
            if (item is MemoDetailUiState.Success) return item
        }
        fail("Expected MemoDetailUiState.Success")
    }

    private suspend fun ReceiveTurbine<MemoDetailUiState>.awaitErrorState(): MemoDetailUiState.Error {
        repeat(4) {
            val item = awaitItem()
            if (item is MemoDetailUiState.Error) return item
        }
        fail("Expected MemoDetailUiState.Error")
    }

    private suspend fun ReceiveTurbine<MemoDetailEvent>.awaitErrorEvent(): MemoDetailEvent.ShowError {
        val event = awaitItem()
        return assertInstanceOf(MemoDetailEvent.ShowError::class.java, event)
    }

    private fun assertStringResource(expected: Int, actual: UiText) {
        val resource = assertInstanceOf(UiText.StringResource::class.java, actual)
        assertEquals(expected, resource.resId)
    }

    private class RecordingMemoRepository : MemoRepository {
        private val memos = MutableStateFlow<List<Memo>>(emptyList())
        val updatedMemos = mutableListOf<Memo>()
        var loadError: Throwable? = null
        var updateError: Throwable? = null

        fun emitMemos(nextMemos: List<Memo>) {
            memos.value = nextMemos
        }

        override fun getMemos(): Flow<List<Memo>> = memos

        override fun getMemo(id: String): Flow<Memo> {
            loadError?.let { error -> return flow { throw error } }
            return memos.map { list -> list.first { it.id == id } }
        }

        override suspend fun createMemo(memo: Memo) = Unit

        override suspend fun updateMemo(memo: Memo) {
            updateError?.let { throw it }
            updatedMemos += memo
        }

        override suspend fun deleteMemo(id: String) = Unit

        override suspend fun getMemoCount(): Int = memos.value.size

        override suspend fun deleteAll() {
            memos.value = emptyList()
        }

        override suspend fun getPendingSyncMemos(): List<Memo> = memos.value.filter { it.pendingSync }
    }
}

private fun sampleMemo(id: String, content: String) = Memo(
    id = id,
    content = content,
    type = MemoType.TEXT,
    createdAt = 1_700_000_000_000,
    updatedAt = 1_700_000_000_000,
)
