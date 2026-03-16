package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IsOnboardingCompletedUseCase @Inject constructor(private val userDataRepository: UserDataRepository) {
    operator fun invoke(): Flow<Boolean> = userDataRepository.userData.map { it.isOnboardingCompleted }
}
