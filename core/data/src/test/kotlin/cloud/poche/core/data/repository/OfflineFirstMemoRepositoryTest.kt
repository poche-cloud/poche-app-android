package cloud.poche.core.data.repository

import cloud.poche.core.database.dao.MemoDao
import cloud.poche.core.database.entity.MemoEntity
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OfflineFirstMemoRepositoryTest {

    private lateinit var memoDao: MemoDao
    private lateinit var repository: OfflineFirstMemoRepository

    @BeforeEach
    fun setUp() {
        memoDao = mockk(relaxed = true)
        repository = OfflineFirstMemoRepository(memoDao)
    }

    @Test
    fun `getMemos returns flow of domain models`() = runTest {
        every { memoDao.getMemos() } returns flowOf(
            listOf(
                textMemoEntity("1", content = "Content 1", pendingSync = true),
                photoMemoEntity(id = "2"),
            ),
        )

        val result = repository.getMemos().first()

        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals(MemoType.TEXT, result[0].type)
        assertTrue(result[0].pendingSync)
        assertEquals("2", result[1].id)
        assertEquals(MemoType.PHOTO, result[1].type)
        assertEquals("/tmp/photo.jpg", result[1].filePath)
    }

    @Test
    fun `getMemo returns mapped domain model`() = runTest {
        every { memoDao.getMemo("1") } returns flowOf(voiceMemoEntity())

        val result = repository.getMemo("1").first()

        assertEquals("1", result.id)
        assertEquals(MemoType.VOICE, result.type)
        assertEquals("/tmp/voice.m4a", result.filePath)
        assertEquals(3000L, result.durationMs)
        assertTrue(result.pendingSync)
    }

    @Test
    fun `createMemo calls dao insert with text memo entity`() = runTest {
        val memo = textMemo()

        repository.createMemo(memo)

        verifyInsertedMemo(memoDao, textMemoEntity("1", pendingSync = true))
    }

    @Test
    fun `createMemo preserves media fields`() = runTest {
        val memo = photoMemo()

        repository.createMemo(memo)

        verifyInsertedMemo(memoDao, photoMemoEntity())
    }

    @Test
    fun `createMemo propagates dao failure`() {
        val error = IllegalStateException("insert failed")
        coEvery { memoDao.insertMemo(any()) } throws error

        assertSame(
            error,
            assertThrows(IllegalStateException::class.java) {
                runTest {
                    repository.createMemo(Memo("1", "Content", MemoType.TEXT, 1000L, 1000L))
                }
            },
        )
    }

    @Test
    fun `updateMemo calls dao update`() = runTest {
        val memo = updatedTextMemo()

        repository.updateMemo(memo)

        verifyUpdatedMemo(memoDao, updatedTextMemoEntity())
    }

    @Test
    fun `deleteMemo calls dao delete`() = runTest {
        repository.deleteMemo("1")

        coVerify { memoDao.deleteMemo("1") }
    }

    @Test
    fun `getMemoCount returns dao count`() = runTest {
        coEvery { memoDao.getMemoCount() } returns 42

        assertEquals(42, repository.getMemoCount())
    }

    @Test
    fun `deleteAll calls dao deleteAll`() = runTest {
        repository.deleteAll()

        coVerify { memoDao.deleteAll() }
    }

    @Test
    fun `getPendingSyncMemos returns domain models`() = runTest {
        coEvery { memoDao.getPendingSyncMemos() } returns listOf(textMemoEntity("1", pendingSync = true))

        val result = repository.getPendingSyncMemos()

        assertEquals(1, result.size)
        assertEquals(true, result[0].pendingSync)
    }

    @Test
    fun `getMemos propagates invalid memo type`() {
        every { memoDao.getMemos() } returns flowOf(
            listOf(MemoEntity("1", "Content", "UNKNOWN", 1000L, 1000L)),
        )

        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                repository.getMemos().first()
            }
        }
    }
}

private fun textMemo(
    id: String = "1",
    content: String = "Content",
    createdAt: Long = 1000L,
    updatedAt: Long = createdAt,
    pendingSync: Boolean = true,
) = Memo(
    id = id,
    content = content,
    type = MemoType.TEXT,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pendingSync = pendingSync,
)

private fun updatedTextMemo() = textMemo(content = "Updated", updatedAt = 2000L)

private fun photoMemo() = Memo(
    id = "photo",
    content = "Caption",
    type = MemoType.PHOTO,
    createdAt = 1000L,
    updatedAt = 2000L,
    filePath = "/tmp/photo.jpg",
    durationMs = 3000L,
    pendingSync = true,
)

private fun textMemoEntity(
    id: String,
    content: String = "Content",
    createdAt: Long = 1000L,
    updatedAt: Long = createdAt,
    pendingSync: Boolean = false,
) = MemoEntity(
    id = id,
    content = content,
    type = "TEXT",
    createdAt = createdAt,
    updatedAt = updatedAt,
    pendingSync = pendingSync,
)

private fun updatedTextMemoEntity() = MemoEntity(
    id = "1",
    content = "Updated",
    type = "TEXT",
    createdAt = 1000L,
    updatedAt = 2000L,
    pendingSync = true,
)

private fun photoMemoEntity(id: String = "photo") = MemoEntity(
    id = id,
    content = "Caption",
    type = "PHOTO",
    createdAt = 1000L,
    updatedAt = 2000L,
    filePath = "/tmp/photo.jpg",
    durationMs = 3000L,
    pendingSync = true,
)

private fun voiceMemoEntity() = MemoEntity(
    id = "1",
    content = "Voice",
    type = "VOICE",
    createdAt = 1000L,
    updatedAt = 2000L,
    filePath = "/tmp/voice.m4a",
    durationMs = 3000L,
    pendingSync = true,
)

private fun verifyInsertedMemo(memoDao: MemoDao, expected: MemoEntity) {
    coVerify { memoDao.insertMemo(expected) }
}

private fun verifyUpdatedMemo(memoDao: MemoDao, expected: MemoEntity) {
    coVerify { memoDao.updateMemo(expected) }
}
