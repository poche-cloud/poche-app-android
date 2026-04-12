package cloud.poche.core.auth

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthManagerTest {

    private class FakeAuthManager : AuthManager {
        private var userId: String? = null

        override suspend fun signInAnonymously(): Result<String> {
            userId = "fake-uid-123"
            return Result.success(userId!!)
        }

        override fun getCurrentUserId(): String? = userId

        override fun signOut() {
            userId = null
        }
    }

    @Test
    fun `signInAnonymously returns uid and sets current user`() = runTest {
        val authManager = FakeAuthManager()
        val result = authManager.signInAnonymously()
        
        assertTrue(result.isSuccess)
        assertEquals("fake-uid-123", result.getOrNull())
        assertEquals("fake-uid-123", authManager.getCurrentUserId())
    }

    @Test
    fun `signOut clears current user`() = runTest {
        val authManager = FakeAuthManager()
        authManager.signInAnonymously()
        authManager.signOut()
        
        assertEquals(null, authManager.getCurrentUserId())
    }
}
