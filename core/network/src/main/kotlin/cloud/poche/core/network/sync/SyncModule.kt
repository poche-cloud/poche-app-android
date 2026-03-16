package cloud.poche.core.network.sync

import cloud.poche.core.domain.repository.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    abstract fun bindSyncManager(impl: SyncManagerImpl): SyncManager
}
