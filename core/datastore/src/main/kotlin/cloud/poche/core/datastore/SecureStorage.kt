package cloud.poche.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SecureStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_storage")

@Singleton
class SecureStorageImpl @Inject constructor(@ApplicationContext private val context: Context) : SecureStorage {

    private val aead: Aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://tink_master_key")
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    override suspend fun putString(key: String, value: String) {
        val encryptedValue = aead.encrypt(value.toByteArray(), null)
        val base64Value = android.util.Base64.encodeToString(encryptedValue, android.util.Base64.DEFAULT)
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = base64Value
        }
    }

    override suspend fun getString(key: String): String? {
        val base64Value = context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)]
        }.first() ?: return null

        return try {
            val encryptedValue = android.util.Base64.decode(base64Value, android.util.Base64.DEFAULT)
            val decryptedValue = aead.decrypt(encryptedValue, null)
            String(decryptedValue)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun remove(key: String) {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
        }
    }
}
