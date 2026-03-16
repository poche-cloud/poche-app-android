package cloud.poche.core.network.sync

import cloud.poche.core.domain.repository.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManagerImpl @Inject constructor() : SyncManager {

    private val _isSyncing = MutableStateFlow(false)

    override suspend fun sync(): Result<Unit> {
        // Stub: actual sync logic deferred until backend API is defined
        return Result.success(Unit)
    }

    override fun isSyncing(): Flow<Boolean> = _isSyncing
}
