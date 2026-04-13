package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.UserDataRepository
import javax.inject.Inject

class SetAiConsentUseCase @Inject constructor(private val userDataRepository: UserDataRepository) {
    suspend operator fun invoke(consented: Boolean) {
        userDataRepository.setAiConsent(consented)
    }
}
