package cloud.poche.core.database.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import app.cash.turbine.test
import cloud.poche.core.database.PocheDatabase
import cloud.poche.core.database.entity.MemoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class MemoDaoTest {

    private lateinit var database: PocheDatabase
    private lateinit var memoDao: MemoDao

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication() as Context
        database = Room.inMemoryDatabaseBuilder(context, PocheDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        memoDao = database.memoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertMemo stores text memo`() = runTest {
        val memo = textMemo(id = "text-1", content = "First")

        memoDao.insertMemo(memo)

        assertEquals(listOf(memo), memoDao.getMemos().first())
    }

    @Test
    fun `getMemos emits changes from newest updatedAt first`() = runTest {
        memoDao.getMemos().test {
            assertEquals(emptyList<MemoEntity>(), awaitItem())

            memoDao.insertMemo(textMemo(id = "old", updatedAt = 1000L))
            assertEquals(listOf("old"), awaitItem().map { it.id })

            memoDao.insertMemo(textMemo(id = "new", updatedAt = 2000L))
            assertEquals(listOf("new", "old"), awaitItem().map { it.id })
        }
    }

    @Test
    fun `insertMemo replaces memo with same id`() = runTest {
        memoDao.insertMemo(textMemo(id = "same", content = "Original"))

        memoDao.insertMemo(textMemo(id = "same", content = "Updated", updatedAt = 2000L))

        val result = memoDao.getMemo("same").first()
        assertEquals("Updated", result.content)
        assertEquals(1, memoDao.getMemoCount())
    }

    @Test
    fun `getMemo returns only requested memo`() = runTest {
        memoDao.insertMemo(textMemo(id = "first"))
        memoDao.insertMemo(textMemo(id = "second"))

        assertEquals("second", memoDao.getMemo("second").first().id)
    }

    @Test
    fun `updateMemo persists changed fields`() = runTest {
        memoDao.insertMemo(textMemo(id = "memo", content = "Before", updatedAt = 1000L))

        memoDao.updateMemo(textMemo(id = "memo", content = "After", updatedAt = 3000L, pendingSync = true))

        val result = memoDao.getMemo("memo").first()
        assertEquals("After", result.content)
        assertEquals(3000L, result.updatedAt)
        assertTrue(result.pendingSync)
    }

    @Test
    fun `deleteMemo removes only matching memo`() = runTest {
        memoDao.insertMemo(textMemo(id = "keep"))
        memoDao.insertMemo(textMemo(id = "delete"))

        memoDao.deleteMemo("delete")

        assertEquals(listOf("keep"), memoDao.getMemos().first().map { it.id })
    }

    @Test
    fun `getMemoCount tracks inserted and deleted memos`() = runTest {
        memoDao.insertMemo(textMemo(id = "one"))
        memoDao.insertMemo(textMemo(id = "two"))
        memoDao.deleteMemo("one")

        assertEquals(1, memoDao.getMemoCount())
    }

    @Test
    fun `deleteAll clears every memo`() = runTest {
        memoDao.insertMemo(textMemo(id = "one"))
        memoDao.insertMemo(textMemo(id = "two"))

        memoDao.deleteAll()

        assertEquals(0, memoDao.getMemoCount())
        assertEquals(emptyList<MemoEntity>(), memoDao.getMemos().first())
    }

    @Test
    fun `getPendingSyncMemos returns only pending records`() = runTest {
        memoDao.insertMemo(textMemo(id = "synced", pendingSync = false))
        memoDao.insertMemo(textMemo(id = "pending", pendingSync = true))

        assertEquals(listOf("pending"), memoDao.getPendingSyncMemos().map { it.id })
    }

    @Test
    fun `media fields roundtrip for photo and voice memos`() = runTest {
        memoDao.insertMemo(photoMemo())
        memoDao.insertMemo(voiceMemo())

        val result = memoDao.getMemos().first().associateBy { it.id }
        assertEquals("/tmp/photo.jpg", result["photo"]?.filePath)
        assertNull(result["photo"]?.durationMs)
        assertEquals("/tmp/voice.m4a", result["voice"]?.filePath)
        assertEquals(4500L, result["voice"]?.durationMs)
    }

    @Test
    fun `migration from version 2 adds pendingSync default`() = runTest {
        val context = RuntimeEnvironment.getApplication() as Context
        val databaseName = "migration-${System.nanoTime()}.db"
        createVersion2Database(context, databaseName)

        val migratedDatabase = Room.databaseBuilder(context, PocheDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()

        try {
            val migratedMemo = migratedDatabase.memoDao().getMemo("legacy").first()
            assertFalse(migratedMemo.pendingSync)
        } finally {
            migratedDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun createVersion2Database(context: Context, databaseName: String) {
        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->
            createVersion2MemosTable(db)
            insertVersion2Memo(db)
            db.version = 2
        }
    }

    private fun createVersion2MemosTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE memos (
                id TEXT NOT NULL PRIMARY KEY,
                content TEXT NOT NULL,
                type TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                filePath TEXT,
                durationMs INTEGER
            )
            """.trimIndent(),
        )
    }

    private fun insertVersion2Memo(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO memos (id, content, type, createdAt, updatedAt, filePath, durationMs)
            VALUES ('legacy', 'Migrated memo', 'TEXT', 1000, 2000, NULL, NULL)
            """.trimIndent(),
        )
    }

    private fun textMemo(
        id: String = "text",
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

    private fun photoMemo() = MemoEntity(
        id = "photo",
        content = "Photo caption",
        type = "PHOTO",
        createdAt = 1000L,
        updatedAt = 3000L,
        filePath = "/tmp/photo.jpg",
    )

    private fun voiceMemo() = MemoEntity(
        id = "voice",
        content = "Voice note",
        type = "VOICE",
        createdAt = 1000L,
        updatedAt = 2000L,
        filePath = "/tmp/voice.m4a",
        durationMs = 4500L,
        pendingSync = true,
    )
}
