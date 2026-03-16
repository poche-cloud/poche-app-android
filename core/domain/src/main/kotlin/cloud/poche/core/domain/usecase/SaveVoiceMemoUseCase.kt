package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.MemoRepository
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import java.util.UUID
import javax.inject.Inject

class SaveVoiceMemoUseCase @Inject constructor(private val memoRepository: MemoRepository) {
    suspend operator fun invoke(filePath: String, durationMs: Long, caption: String = "") {
        val now = System.currentTimeMillis()
        val memo = Memo(
            id = UUID.randomUUID().toString(),
            content = caption,
            type = MemoType.VOICE,
            createdAt = now,
            updatedAt = now,
            filePath = filePath,
            durationMs = durationMs,
        )
        memoRepository.createMemo(memo)
    }
}
