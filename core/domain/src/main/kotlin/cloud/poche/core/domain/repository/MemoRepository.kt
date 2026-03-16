package cloud.poche.core.domain.repository

import cloud.poche.core.model.Memo
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    fun getMemos(): Flow<List<Memo>>
    fun getMemo(id: String): Flow<Memo>
    suspend fun createMemo(memo: Memo)
    suspend fun updateMemo(memo: Memo)
    suspend fun deleteMemo(id: String)
    suspend fun getMemoCount(): Int
    suspend fun deleteAll()
}
