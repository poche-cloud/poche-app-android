package cloud.poche.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Memo(
    val id: String,
    val content: String,
    val type: MemoType,
    val createdAt: Long,
    val updatedAt: Long,
    val filePath: String? = null,
    val durationMs: Long? = null,
    val pendingSync: Boolean = false,
)

enum class MemoType {
    TEXT,
    PHOTO,
    VOICE,
}
