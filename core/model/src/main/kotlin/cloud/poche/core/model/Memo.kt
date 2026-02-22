package cloud.poche.core.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Memo(
    val id: String,
    val content: String,
    val type: MemoType,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class MemoType {
    TEXT,
    PHOTO,
    VOICE,
}
