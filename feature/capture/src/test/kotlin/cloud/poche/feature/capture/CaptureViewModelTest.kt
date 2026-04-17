package cloud.poche.feature.capture

import app.cash.turbine.test
import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.domain.usecase.SaveMemoUseCase
import cloud.poche.core.domain.usecase.SavePhotoMemoUseCase
import cloud.poche.core.domain.usecase.SaveVoiceMemoUseCase
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.R
import cloud.poche.core.ui.UiText
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
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
    fun `saveMemo trims content and stores pending sync text memo`() = runTest {
        viewModel.events.test {
            viewModel.saveMemo("  note body  ")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(CaptureEvent.SaveSuccess, awaitItem())
            val memo = repository.createdMemos.single()
            assertEquals("note body", memo.content)
            assertEquals(MemoType.TEXT, memo.type)
            assertTrue(memo.pendingSync)
            assertFalse(viewModel.isSaving.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveMemo emits validation error for blank content`() = runTest {
        viewModel.events.test {
            viewModel.saveMemo("   ")
            testDispatcher.scheduler.advanceUntilIdle()

            assertStringResource(R.string.capture_error_empty_input, awaitErrorEvent().message)
            assertTrue(repository.createdMemos.isEmpty())
            assertFalse(viewModel.isSaving.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveMemo emits save error and clears saving state`() = runTest {
        repository.createError = IllegalStateException("create failed")

        viewModel.events.test {
            viewModel.saveMemo("memo")
            testDispatcher.scheduler.advanceUntilIdle()

            assertStringResource(R.string.capture_error_save_failed, awaitErrorEvent().message)
            assertFalse(viewModel.isSaving.value)
            cancelAndIgnoreRemainingEvents()
        }
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
            assertFalse(viewModel.isSaving.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveBookmarkFromUrl emits validation error for invalid shared text`() = runTest {
        viewModel.events.test {
            viewModel.saveBookmarkFromUrl("not a url")
            testDispatcher.scheduler.advanceUntilIdle()

            assertStringResource(R.string.capture_error_empty_input, awaitErrorEvent().message)
            assertTrue(repository.createdMemos.isEmpty())
            assertFalse(viewModel.isSaving.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPhotoCaptured moves photo state to preview and clearPhoto returns to picker`() {
        val uri = mockUri("content://photo/1")

        viewModel.onPhotoCaptured(uri)
        assertEquals(PhotoCaptureUiState.Preview(uri), viewModel.photoUiState.value)

        viewModel.clearPhoto()
        assertEquals(PhotoCaptureUiState.Picker, viewModel.photoUiState.value)
    }

    @Test
    fun `savePhoto stores photo memo and stays saving until navigation`() = runTest {
        val uri = mockUri("content://photo/1")

        viewModel.events.test {
            viewModel.savePhoto(uri, "  photo caption  ")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(CaptureEvent.SaveSuccess, awaitItem())
            val memo = repository.createdMemos.single()
            assertEquals(MemoType.PHOTO, memo.type)
            assertEquals("content://photo/1", memo.filePath)
            assertEquals("photo caption", memo.content)
            assertTrue(memo.pendingSync)
            assertEquals(PhotoCaptureUiState.Saving, viewModel.photoUiState.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `savePhoto restores preview and emits error when save fails`() = runTest {
        val uri = mockUri("content://photo/1")
        repository.createError = IllegalStateException("photo failed")

        viewModel.events.test {
            viewModel.savePhoto(uri, "caption")
            testDispatcher.scheduler.advanceUntilIdle()

            assertStringResource(R.string.capture_error_save_failed, awaitErrorEvent().message)
            assertEquals(PhotoCaptureUiState.Preview(uri), viewModel.photoUiState.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveVoiceMemo stores voice memo and stays saving until navigation`() = runTest {
        viewModel.events.test {
            viewModel.saveVoiceMemo("/tmp/voice.m4a", 3_000, "  voice caption  ")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(CaptureEvent.SaveSuccess, awaitItem())
            val memo = repository.createdMemos.single()
            assertEquals(MemoType.VOICE, memo.type)
            assertEquals("/tmp/voice.m4a", memo.filePath)
            assertEquals(3_000, memo.durationMs)
            assertEquals("voice caption", memo.content)
            assertTrue(memo.pendingSync)
            assertEquals(VoiceCaptureUiState.Saving, viewModel.voiceUiState.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveVoiceMemo restores recorded state and emits error when save fails`() = runTest {
        repository.createError = IllegalStateException("voice failed")

        viewModel.events.test {
            viewModel.saveVoiceMemo("/tmp/voice.m4a", 3_000, "caption")
            testDispatcher.scheduler.advanceUntilIdle()

            assertStringResource(R.string.capture_error_save_failed, awaitErrorEvent().message)
            assertEquals(
                VoiceCaptureUiState.Recorded("/tmp/voice.m4a", 3_000),
                viewModel.voiceUiState.value,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun mockUri(value: String): android.net.Uri {
        val uri = io.mockk.mockk<android.net.Uri>()
        io.mockk.every { uri.toString() } returns value
        return uri
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<CaptureEvent>.awaitErrorEvent(): CaptureEvent.ShowError {
        val event = awaitItem()
        return assertInstanceOf(CaptureEvent.ShowError::class.java, event)
    }

    private fun assertStringResource(expected: Int, actual: UiText) {
        val resource = assertInstanceOf(UiText.StringResource::class.java, actual)
        assertEquals(expected, resource.resId)
    }

    private class RecordingMemoRepository : MemoRepository {
        val createdMemos = mutableListOf<Memo>()
        var createError: Throwable? = null

        override fun getMemos(): Flow<List<Memo>> = flowOf(createdMemos)

        override fun getMemo(id: String): Flow<Memo> = flowOf(createdMemos.first { it.id == id })

        override suspend fun createMemo(memo: Memo) {
            createError?.let { throw it }
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
