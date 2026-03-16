package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.UserDataRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke() =
        userDataRepository.setOnboardingCompleted(true)
}
