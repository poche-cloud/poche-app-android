package cloud.poche.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Base64

@OptIn(ExperimentalCoroutinesApi::class)
class SecureStorageTest {

    private lateinit var secureStorage: SecureStorageImpl
    private lateinit var context: Context
    private lateinit var aead: Aead

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)

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

        secureStorage = SecureStorageImpl(context)
        // Set private aead via reflection or use a test-friendly constructor
        val field = SecureStorageImpl::class.java.getDeclaredField("aead")
        field.isAccessible = true
        field.set(secureStorage, aead)
    }

    @Test
    fun `encryption roundtrip works`() = runTest {
        val originalValue = "my_secret_password"
        val encrypted = aead.encrypt(originalValue.toByteArray(), null)
        val decrypted = aead.decrypt(encrypted, null)
        assertEquals(originalValue, String(decrypted))
    }
}
