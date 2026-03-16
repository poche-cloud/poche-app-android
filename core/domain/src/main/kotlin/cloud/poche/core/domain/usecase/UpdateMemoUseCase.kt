package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.model.Memo
import javax.inject.Inject

class UpdateMemoUseCase @Inject constructor(private val memoRepository: MemoRepository) {
    suspend operator fun invoke(memo: Memo) = memoRepository.updateMemo(memo)
}
