package cloud.poche.core.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import cloud.poche.core.datastore.SecureStorage

interface AuthManager {
    suspend fun signInAnonymously(): Result<String>
    fun getCurrentUserId(): String?
    fun signOut()
}

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val secureStorage: SecureStorage
) : AuthManager {

    override suspend fun signInAnonymously(): Result<String> = try {
        val result = firebaseAuth.signInAnonymously().await()
        val user = result.user ?: throw Exception("Sign in failed: No user")
        val token = user.getIdToken(false).await().token ?: ""
        
        // Store token for subsequent API calls
        secureStorage.putString("auth_token", token)
        
        Result.success(user.uid)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
