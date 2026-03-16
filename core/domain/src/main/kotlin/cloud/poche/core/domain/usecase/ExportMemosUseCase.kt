package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.model.Memo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ExportMemosUseCase @Inject constructor(
    private val memoRepository: MemoRepository,
) {
    suspend operator fun invoke(): String {
        val memos = memoRepository.getMemos().first()
        return Json.encodeToString(memos)
    }
}
