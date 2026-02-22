package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.UserDataRepository
import javax.inject.Inject

class SetLocaleUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(locale: String) = userDataRepository.setLocale(locale)
}
