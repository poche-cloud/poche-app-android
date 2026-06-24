package cloud.poche.feature.home

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.domain.usecase.DeleteMemoUseCase
import cloud.poche.core.domain.usecase.GetMemosUseCase
import cloud.poche.core.domain.usecase.SaveMemoUseCase
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.R
import cloud.poche.core.ui.UiText
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RecordingMemoRepository
    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = RecordingMemoRepository()
        viewModel = createViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState starts with loading state`() {
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState maps repository memos to success`() = runTest {
        val memo = sampleMemo("memo-1", "first")
        repository.emitMemos(listOf(memo))

        viewModel.uiState.test {
            val success = awaitSuccess()

            assertEquals(listOf(memo), success.memos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState maps repository errors to dynamic error`() = runTest {
        repository.loadError = IllegalStateException("load failed")
        viewModel = createViewModel()

        viewModel.uiState.test {
            val error = awaitErrorState()

            assertEquals("load failed", (error.message as UiText.DynamicString).value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry reloads memos after an error`() = runTest {
        repository.loadError = IllegalStateException("first load failed")
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitErrorState()

            repository.loadError = null
            repository.emitMemos(listOf(sampleMemo("memo-2", "after retry")))
            viewModel.retry()

            assertEquals("memo-2", awaitSuccess().memos.single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMemo deletes memo by id`() = runTest {
        viewModel.deleteMemo("memo-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("memo-1"), repository.deletedMemoIds)
    }

    @Test
    fun `deleteMemo emits error when deletion fails`() = runTest {
        repository.deleteError = IllegalStateException("delete failed")

        viewModel.events.test {
            viewModel.deleteMemo("memo-1")
            testDispatcher.scheduler.advanceUntilIdle()

            assertStringResource(R.string.error_delete_failed, awaitErrorEvent().message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `quickCapture trims content and emits capture success`() = runTest {
        viewModel.events.test {
            viewModel.quickCapture("  hello memo  ")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(HomeEvent.CaptureSuccess, awaitItem())
            val memo = repository.createdMemos.single()
            assertEquals("hello memo", memo.content)
            assertEquals(MemoType.TEXT, memo.type)
            assertTrue(memo.pendingSync)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel() = HomeViewModel(
        getMemosUseCase = GetMemosUseCase(repository),
        deleteMemoUseCase = DeleteMemoUseCase(repository),
        saveMemoUseCase = SaveMemoUseCase(repository),
    )

    private suspend fun ReceiveTurbine<HomeUiState>.awaitSuccess(): HomeUiState.Success {
        repeat(4) {
            val item = awaitItem()
            if (item is HomeUiState.Success) return item
        }
        fail("Expected HomeUiState.Success")
    }

    private suspend fun ReceiveTurbine<HomeUiState>.awaitErrorState(): HomeUiState.Error {
        repeat(4) {
            val item = awaitItem()
            if (item is HomeUiState.Error) return item
        }
        fail("Expected HomeUiState.Error")
    }

    private suspend fun ReceiveTurbine<HomeEvent>.awaitErrorEvent(): HomeEvent.ShowError {
        val event = awaitItem()
        return assertInstanceOf(HomeEvent.ShowError::class.java, event)
    }

    private fun assertStringResource(expected: Int, actual: UiText) {
        val resource = assertInstanceOf(UiText.StringResource::class.java, actual)
        assertEquals(expected, resource.resId)
    }

    private class RecordingMemoRepository : MemoRepository {
        private val memos = MutableStateFlow<List<Memo>>(emptyList())
        val createdMemos = mutableListOf<Memo>()
        val deletedMemoIds = mutableListOf<String>()
        var loadError: Throwable? = null
        var deleteError: Throwable? = null

        fun emitMemos(nextMemos: List<Memo>) {
            memos.value = nextMemos
        }

        override fun getMemos(): Flow<List<Memo>> {
            loadError?.let { error -> return flow { throw error } }
            return memos
        }

        override fun getMemo(id: String): Flow<Memo> = memos.map { list -> list.first { it.id == id } }

        override suspend fun createMemo(memo: Memo) {
            createdMemos += memo
        }

        override suspend fun updateMemo(memo: Memo) = Unit

        override suspend fun deleteMemo(id: String) {
            deleteError?.let { throw it }
            deletedMemoIds += id
        }

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
