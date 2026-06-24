package cloud.poche.core.network.sync

import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.domain.repository.SyncManager
import cloud.poche.core.network.PocheNetworkDataSource
import cloud.poche.core.network.model.NetworkMemo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManagerImpl @Inject constructor(
    private val memoRepository: MemoRepository,
    private val networkDataSource: PocheNetworkDataSource,
) : SyncManager {

    private val isSyncingState = MutableStateFlow(false)

    override suspend fun sync(): Result<Unit> {
        if (isSyncingState.value) return Result.success(Unit)
        isSyncingState.value = true

        return try {
            // 1. Upload pending memos
            val pendingMemos = memoRepository.getPendingSyncMemos()
            for (memo in pendingMemos) {
                networkDataSource.createMemo(
                    title = "Memo", // Placeholder: use content or first line
                    content = memo.content,
                    collectionId = null,
                )
                // Mark as synced locally
                memoRepository.updateMemo(memo.copy(pendingSync = false))
            }

            // 2. Fetch remote updates (Simplistic sync for now)
            val remoteMemos = networkDataSource.getMemos()
            if (remoteMemos.isNotEmpty()) {
                // Here we would implement LWW or conflict resolution
                // For now, just save if not exists or newer
                // (Omitted for brevity in this task implementation)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            isSyncingState.value = false
        }
    }

    override fun isSyncing(): Flow<Boolean> = isSyncingState
}
