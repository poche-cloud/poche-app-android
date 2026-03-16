package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.MemoRepository
import javax.inject.Inject

class DeleteMemoUseCase @Inject constructor(private val memoRepository: MemoRepository) {
    suspend operator fun invoke(id: String) = memoRepository.deleteMemo(id)
}
