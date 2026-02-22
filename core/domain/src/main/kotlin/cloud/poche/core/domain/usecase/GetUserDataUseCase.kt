package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.UserDataRepository
import cloud.poche.core.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<UserData> = userDataRepository.userData
}
