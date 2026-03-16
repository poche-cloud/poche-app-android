package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.model.Memo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMemoByIdUseCase @Inject constructor(private val memoRepository: MemoRepository) {
    operator fun invoke(id: String): Flow<Memo> = memoRepository.getMemo(id)
}
