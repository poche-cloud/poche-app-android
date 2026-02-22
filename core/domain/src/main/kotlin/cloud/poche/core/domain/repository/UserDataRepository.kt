package cloud.poche.core.domain.repository

import cloud.poche.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
