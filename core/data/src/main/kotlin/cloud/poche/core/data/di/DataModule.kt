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

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindMemoRepository(impl: OfflineFirstMemoRepository): MemoRepository

    @Binds
    abstract fun bindUserDataRepository(impl: OfflineFirstUserDataRepository): UserDataRepository

    @Binds
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository
}
