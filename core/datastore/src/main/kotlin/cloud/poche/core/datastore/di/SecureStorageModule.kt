package cloud.poche.core.datastore.di

import android.content.Context
import cloud.poche.core.datastore.RealSecureStorage
import cloud.poche.core.datastore.SecureStorage
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecureStorageModule {
    @Binds
    @Singleton
    abstract fun bindSecureStorage(realSecureStorage: RealSecureStorage): SecureStorage

    companion object {
        @Provides
        @Singleton
        fun provideAead(@ApplicationContext context: Context): Aead {
            AeadConfig.register()
            return AndroidKeysetManager.Builder()
                .withSharedPref(context, "poche_keyset", "poche_secure_prefs")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://poche_master_key")
                .build()
                .keysetHandle
                .getPrimitive(Aead::class.java)
        }
    }
}
