package cloud.poche.core.data.di

import cloud.poche.core.data.repository.FileRepository
import cloud.poche.core.data.repository.FileRepositoryImpl
import cloud.poche.core.data.repository.OfflineFirstMemoRepository
import cloud.poche.core.data.repository.OfflineFirstUserDataRepository
import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.domain.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFileRepository(fileRepositoryImpl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindMemoRepository(offlineFirstMemoRepository: OfflineFirstMemoRepository): MemoRepository

    @Binds
    @Singleton
    abstract fun bindUserDataRepository(
        offlineFirstUserDataRepository: OfflineFirstUserDataRepository,
    ): UserDataRepository
}
