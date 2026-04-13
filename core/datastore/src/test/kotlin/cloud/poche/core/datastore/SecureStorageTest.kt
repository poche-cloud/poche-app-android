package cloud.poche.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Base64

class SecureStorageTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var secureStorage: SecureStorage
    private lateinit var aead: Aead

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupTink() {
            AeadConfig.register()
        }
    }

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        // Mock android.util.Base64
        mockkStatic(android.util.Base64::class)
        every { android.util.Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg())
        }
        every { android.util.Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }

        val keysetHandle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        aead = keysetHandle.getPrimitive(Aead::class.java)

        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempDir, "test.preferences_pb") }
        )

        secureStorage = SecureStorageImpl(dataStore, aead)
    }

    @Test
    fun `test putString and getString roundtrip`() = runTest {
        secureStorage.putString("my_key", "my_secret_value")
        val result = secureStorage.getString("my_key")
        assertEquals("my_secret_value", result)
    }

    @Test
    fun `test getString returns null when key not found`() = runTest {
        val result = secureStorage.getString("non_existent_key")
        assertNull(result)
    }

    @Test
    fun `test remove deletes key`() = runTest {
        secureStorage.putString("temp_key", "value")
        secureStorage.remove("temp_key")
        val result = secureStorage.getString("temp_key")
        assertNull(result)
    }
}
