package cloud.poche.core.domain.usecase

import app.cash.turbine.test
import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetMemosUseCaseTest {

    @Test
    fun `invoke emits repository memos`() = runTest {
        val memos = listOf(textMemo("1"), textMemo("2"))
        val useCase = getMemosUseCase(flowOf(memos))

        useCase().test {
            assertEquals(memos, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke preserves repository ordering`() = runTest {
        val memos = listOf(textMemo("new", updatedAt = 3000L), textMemo("old", updatedAt = 1000L))
        val useCase = getMemosUseCase(flowOf(memos))

        useCase().test {
            assertMemoIds(listOf("new", "old"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke forwards subsequent repository emissions`() = runTest {
        val updates = MutableSharedFlow<List<Memo>>()
        val useCase = getMemosUseCase(updates)

        useCase().test {
            updates.emitMemos("1")
            assertMemoIds(listOf("1"), awaitItem())

            updates.emitMemos("1", "2")
            assertMemoIds(listOf("1", "2"), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke emits empty list`() = runTest {
        val useCase = getMemosUseCase()

        useCase().test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `invoke propagates repository error`() {
        val error = IllegalStateException("database unavailable")
        val useCase = failingGetMemosUseCase(error)

        assertSame(
            error,
            assertThrows(IllegalStateException::class.java) {
                runTest {
                    useCase().first()
                }
            },
        )
    }
}

class SaveMemoUseCaseTest {

    @Test
    fun `invoke creates memo through repository`() = runTest {
        val repository = RecordingMemoRepository()
        val memo = textMemo("1")
        val useCase = SaveMemoUseCase(repository)

        useCase(memo)

        assertEquals(listOf(memo.copy(pendingSync = true)), repository.createdMemos)
    }

    @Test
    fun `invoke marks text memo as pending sync`() = runTest {
        val repository = RecordingMemoRepository()
        val memo = textMemo("1", pendingSync = false)
        val useCase = SaveMemoUseCase(repository)

        useCase(memo)

        assertTrue(repository.createdMemos.single().pendingSync)
    }

    @Test
    fun `invoke keeps already pending memo pending`() = runTest {
        val repository = RecordingMemoRepository()
        val memo = textMemo("1", pendingSync = true)
        val useCase = SaveMemoUseCase(repository)

        useCase(memo)

        assertTrue(repository.createdMemos.single().pendingSync)
    }

    @Test
    fun `invoke preserves memo content`() = runTest {
        val repository = RecordingMemoRepository()
        val memo = textMemo("1", content = " trimmed content ")
        val useCase = SaveMemoUseCase(repository)

        useCase(memo)

        assertEquals(" trimmed content ", repository.createdMemos.single().content)
    }

    @Test
    fun `invoke preserves memo timestamps`() = runTest {
        val repository = RecordingMemoRepository()
        val memo = textMemo("1", createdAt = 1000L, updatedAt = 2000L)
        val useCase = SaveMemoUseCase(repository)

        useCase(memo)

        assertEquals(1000L, repository.createdMemos.single().createdAt)
        assertEquals(2000L, repository.createdMemos.single().updatedAt)
    }

    @Test
    fun `invoke preserves media metadata`() = runTest {
        val repository = RecordingMemoRepository()
        val memo = photoMemo()
        val useCase = SaveMemoUseCase(repository)

        useCase(memo)

        val created = repository.createdMemos.single()
        assertEquals(photoMemo().filePath, created.filePath)
        assertEquals(photoMemo().durationMs, created.durationMs)
    }

    @Test
    fun `invoke propagates repository failure`() {
        val error = IllegalStateException("write failed")
        val repository = RecordingMemoRepository(createError = error)
        val useCase = SaveMemoUseCase(repository)

        assertSame(
            error,
            assertThrows(IllegalStateException::class.java) {
                runTest {
                    useCase(textMemo("1"))
                }
            },
        )
    }
}

private class RecordingMemoRepository(
    private val memosFlow: Flow<List<Memo>> = flowOf(emptyList()),
    private val createError: RuntimeException? = null,
) : MemoRepository {

    val createdMemos = mutableListOf<Memo>()

    override fun getMemos(): Flow<List<Memo>> = memosFlow

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

private fun textMemo(
    id: String,
    content: String = "Content $id",
    createdAt: Long = 1000L,
    updatedAt: Long = createdAt,
    pendingSync: Boolean = false,
) = Memo(
    id = id,
    content = content,
    type = MemoType.TEXT,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pendingSync = pendingSync,
)

private fun getMemosUseCase(memosFlow: Flow<List<Memo>> = flowOf(emptyList())) =
    GetMemosUseCase(RecordingMemoRepository(memosFlow = memosFlow))

private fun failingGetMemosUseCase(error: RuntimeException) =
    GetMemosUseCase(RecordingMemoRepository(memosFlow = flow { throw error }))

private suspend fun MutableSharedFlow<List<Memo>>.emitMemos(vararg ids: String) {
    emit(ids.map(::textMemo))
}

private fun assertMemoIds(expected: List<String>, memos: List<Memo>) {
    assertEquals(expected, memos.map(Memo::id))
}

private fun photoMemo() = Memo(
    id = "photo",
    content = "caption",
    type = MemoType.PHOTO,
    createdAt = 1000L,
    updatedAt = 2000L,
    filePath = "/tmp/photo.jpg",
    durationMs = 3000L,
)
