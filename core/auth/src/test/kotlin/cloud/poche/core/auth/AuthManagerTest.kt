package cloud.poche.core.auth

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AuthManagerTest {

    private class FakeAuthManager : AuthManager {
        private val _isSignedIn = MutableStateFlow(false)
        override val isSignedIn = _isSignedIn.asStateFlow()

        private var userId: String? = null
        override val currentUserId: String?
            get() = userId

        override suspend fun signInAnonymously(): Result<String> {
            userId = "fake-uid-123"
            _isSignedIn.value = true
            return Result.success(userId!!)
        }

        override suspend fun signOut() {
            userId = null
            _isSignedIn.value = false
        }

        override suspend fun deleteAccount() {
            userId = null
            _isSignedIn.value = false
        }
    }

    @Test
    fun `signInAnonymously returns uid and updates state`() = runTest {
        val authManager = FakeAuthManager()

        authManager.isSignedIn.test {
            assertFalse(awaitItem())

            val result = authManager.signInAnonymously()

            assertTrue(result.isSuccess)
            assertEquals("fake-uid-123", result.getOrNull())
            assertEquals("fake-uid-123", authManager.currentUserId)
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `signOut clears current user and updates state`() = runTest {
        val authManager = FakeAuthManager()
        authManager.signInAnonymously()

        authManager.isSignedIn.test {
            assertTrue(awaitItem())

            authManager.signOut()

            assertNull(authManager.currentUserId)
            assertFalse(awaitItem())
        }
    }
}
