package cloud.poche.core.remoteconfig.di

import cloud.poche.core.domain.repository.RemoteConfigManager
import cloud.poche.core.remoteconfig.FirebaseRemoteConfigManager
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import cloud.poche.core.remoteconfig.R
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteConfigModule {
    @Binds
    abstract fun bindRemoteConfigManager(impl: FirebaseRemoteConfigManager): RemoteConfigManager

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig =
            Firebase.remoteConfig.apply {
                setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = 3600
                    },
                )
                setDefaultsAsync(R.xml.remote_config_defaults)
            }
    }
}
