package cloud.poche.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Base64

@OptIn(ExperimentalCoroutinesApi::class)
class SecureStorageTest {

    private lateinit var secureStorage: SecureStorage
    private lateinit var context: Context
    private lateinit var aead: Aead
    private lateinit var testDataStore: DataStore<Preferences>

    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tempDir, "test.preferences_pb") },
        )

        AeadConfig.register()
        val keysetHandle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        aead = keysetHandle.getPrimitive(Aead::class.java)

        // Mock Base64 because standard Android Base64 doesn't work in JUnit tests
        mockkStatic(android.util.Base64::class)
        every { android.util.Base64.encodeToString(any(), any()) } answers {
            Base64.getEncoder().encodeToString(firstArg<ByteArray>())
        }
        every { android.util.Base64.decode(any<String>(), any()) } answers {
            Base64.getDecoder().decode(firstArg<String>())
        }

        secureStorage = RealSecureStorage(context, aead, testDataStore)
    }

    @Test
    fun `putString and getString roundtrip works`() = runTest {
        val key = "secret_key"
        val value = "secret_value"

        secureStorage.putString(key, value)
        val loadedValue = secureStorage.getString(key)

        assertEquals(value, loadedValue)
    }

    @Test
    fun `getString returns null for non-existent key`() = runTest {
        val loadedValue = secureStorage.getString("non_existent")
        assertNull(loadedValue)
    }

    @Test
    fun `remove deletes value`() = runTest {
        val key = "key_to_delete"
        val value = "value"

        secureStorage.putString(key, value)
        secureStorage.remove(key)

        val loadedValue = secureStorage.getString(key)
        assertNull(loadedValue)
    }
}
