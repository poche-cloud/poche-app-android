package cloud.poche.core.datastore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecureStorageModule {
    @Binds
    @Singleton
    abstract fun bindSecureStorage(
        secureStorageImpl: SecureStorageImpl
    ): SecureStorage
}
