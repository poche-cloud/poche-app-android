package cloud.poche.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cloud.poche.core.database.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY updatedAt DESC")
    fun getMemos(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE id = :id")
    fun getMemo(id: String): Flow<MemoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity)

    @Update
    suspend fun updateMemo(memo: MemoEntity)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteMemo(id: String)

    @Query("SELECT COUNT(*) FROM memos")
    suspend fun getMemoCount(): Int

    @Query("DELETE FROM memos")
    suspend fun deleteAll()

    @Query("SELECT * FROM memos WHERE pendingSync = 1")
    fun getPendingSyncMemos(): Flow<List<MemoEntity>>
}
