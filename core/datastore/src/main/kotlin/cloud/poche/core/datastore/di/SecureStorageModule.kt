package cloud.poche.core.datastore.di

import cloud.poche.core.datastore.SecureStorage
import cloud.poche.core.datastore.SecureStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SecureStorageModule {

    @Binds
    abstract fun bindSecureStorage(impl: SecureStorageImpl): SecureStorage
}
