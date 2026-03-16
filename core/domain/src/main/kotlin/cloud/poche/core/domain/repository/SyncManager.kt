package cloud.poche.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SyncManager {
    suspend fun sync(): Result<Unit>
    fun isSyncing(): Flow<Boolean>
}
