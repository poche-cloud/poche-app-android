package cloud.poche.core.data.repository

import cloud.poche.core.database.dao.MemoDao
import cloud.poche.core.database.entity.MemoEntity
import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstMemoRepository @Inject constructor(
    private val memoDao: MemoDao,
) : MemoRepository {

    override fun getMemos(): Flow<List<Memo>> =
        memoDao.getMemos().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getMemo(id: String): Flow<Memo> =
        memoDao.getMemo(id).map { it.toDomain() }

    override suspend fun createMemo(memo: Memo) {
        memoDao.insertMemo(memo.toEntity())
    }

    override suspend fun updateMemo(memo: Memo) {
        memoDao.updateMemo(memo.toEntity())
    }

    override suspend fun deleteMemo(id: String) {
        memoDao.deleteMemo(id)
    }
}

private fun MemoEntity.toDomain() = Memo(
    id = id,
    content = content,
    type = MemoType.valueOf(type),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun Memo.toEntity() = MemoEntity(
    id = id,
    content = content,
    type = type.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
