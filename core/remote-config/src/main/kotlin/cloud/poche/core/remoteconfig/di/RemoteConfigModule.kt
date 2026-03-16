package cloud.poche.core.remoteconfig.di

import cloud.poche.core.domain.repository.RemoteConfigManager
import cloud.poche.core.remoteconfig.FirebaseRemoteConfigManager
import cloud.poche.core.remoteconfig.R
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteConfigModule {
    @Binds
    abstract fun bindRemoteConfigManager(impl: FirebaseRemoteConfigManager): RemoteConfigManager

    companion object {
        private const val MINIMUM_FETCH_INTERVAL_SECONDS = 3600L

        @Provides
        @Singleton
        fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = Firebase.remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = MINIMUM_FETCH_INTERVAL_SECONDS
                },
            )
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }
}
