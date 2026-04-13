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
        val entities = listOf(
            MemoEntity("1", "Content 1", "TEXT", 1000L, 1000L),
            MemoEntity("2", "Content 2", "PHOTO", 2000L, 2000L),
        )
        every { memoDao.getMemos() } returns flowOf(entities)

        val result = repository.getMemos().first()

        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals(MemoType.TEXT, result[0].type)
        assertEquals("2", result[1].id)
        assertEquals(MemoType.PHOTO, result[1].type)
    }

    @Test
    fun `createMemo calls dao insert`() = runTest {
        val memo = Memo("1", "Content", MemoType.TEXT, 1000L, 1000L)

        repository.createMemo(memo)

        coVerify {
            memoDao.insertMemo(
                MemoEntity("1", "Content", "TEXT", 1000L, 1000L),
            )
        }
    }

    @Test
    fun `getPendingSyncMemos returns domain models`() = runTest {
        val entities = listOf(
            MemoEntity("1", "Pending", "TEXT", 1000L, 1000L, pendingSync = true),
        )
        coEvery { memoDao.getPendingSyncMemos() } returns entities

        val result = repository.getPendingSyncMemos()

        assertEquals(1, result.size)
        assertEquals(true, result[0].pendingSync)
    }
}
