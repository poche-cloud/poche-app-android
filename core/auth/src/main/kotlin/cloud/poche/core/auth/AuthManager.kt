package cloud.poche.core.auth

import kotlinx.coroutines.flow.Flow

interface AuthManager {
    val isSignedIn: Flow<Boolean>
    val currentUserId: String?
    suspend fun signInAnonymously(): Result<String>
    suspend fun signOut()
    suspend fun deleteAccount()
}
