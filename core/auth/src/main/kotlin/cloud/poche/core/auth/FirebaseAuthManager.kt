package cloud.poche.core.auth

import cloud.poche.core.datastore.SecureStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val secureStorage: SecureStorage,
) : AuthManager {

    override val isSignedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun signInAnonymously(): Result<String> = runCatching {
        val result = firebaseAuth.signInAnonymously().await()
        val user = result.user ?: error("User is null")

        // IDトークンを取得して SecureStorage に保存
        val tokenResult = user.getIdToken(true).await()
        val token = tokenResult.token ?: error("Token is null")
        secureStorage.putString("firebase_id_token", token)

        user.uid
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        secureStorage.remove("firebase_id_token")
    }

    override suspend fun deleteAccount() {
        val user = firebaseAuth.currentUser ?: error("No user signed in")
        user.delete().await()
        secureStorage.remove("firebase_id_token")
    }
}
