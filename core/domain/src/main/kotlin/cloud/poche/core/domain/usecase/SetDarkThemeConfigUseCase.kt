package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.UserDataRepository
import cloud.poche.core.model.DarkThemeConfig
import javax.inject.Inject

class SetDarkThemeConfigUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(config: DarkThemeConfig) =
        userDataRepository.setDarkThemeConfig(config)
}
