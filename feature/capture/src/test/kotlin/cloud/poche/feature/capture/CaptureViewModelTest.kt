package cloud.poche.feature.capture

import app.cash.turbine.test
import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.domain.usecase.SaveMemoUseCase
import cloud.poche.core.domain.usecase.SavePhotoMemoUseCase
import cloud.poche.core.domain.usecase.SaveVoiceMemoUseCase
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RecordingMemoRepository
    private lateinit var viewModel: CaptureViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = RecordingMemoRepository()
        viewModel = CaptureViewModel(
            saveMemoUseCase = SaveMemoUseCase(repository),
            savePhotoMemoUseCase = SavePhotoMemoUseCase(repository),
            saveVoiceMemoUseCase = SaveVoiceMemoUseCase(repository),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveBookmarkFromUrl stores URL as pending sync text memo`() = runTest {
        viewModel.events.test {
            viewModel.saveBookmarkFromUrl("https://example.com/articles/123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(CaptureEvent.SaveSuccess, awaitItem())
            val memo = repository.createdMemos.single()
            assertEquals("https://example.com/articles/123", memo.content)
            assertEquals(MemoType.TEXT, memo.type)
            assertTrue(memo.pendingSync)
        }
    }

    private class RecordingMemoRepository : MemoRepository {
        val createdMemos = mutableListOf<Memo>()

        override fun getMemos(): Flow<List<Memo>> = flowOf(createdMemos)

        override fun getMemo(id: String): Flow<Memo> = flowOf(createdMemos.first { it.id == id })

        override suspend fun createMemo(memo: Memo) {
            createdMemos += memo
        }

        override suspend fun updateMemo(memo: Memo) = Unit

        override suspend fun deleteMemo(id: String) = Unit

        override suspend fun getMemoCount(): Int = createdMemos.size

        override suspend fun deleteAll() {
            createdMemos.clear()
        }

        override suspend fun getPendingSyncMemos(): List<Memo> = createdMemos.filter { it.pendingSync }
    }
}
