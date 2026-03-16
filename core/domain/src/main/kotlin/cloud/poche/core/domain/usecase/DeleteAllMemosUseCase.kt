package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.MemoRepository
import javax.inject.Inject

class DeleteAllMemosUseCase @Inject constructor(
    private val memoRepository: MemoRepository,
) {
    suspend operator fun invoke() =
        memoRepository.deleteAll()
}
