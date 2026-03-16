package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDefaultCaptureTypeUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<String?> =
        userDataRepository.defaultCaptureType
}
