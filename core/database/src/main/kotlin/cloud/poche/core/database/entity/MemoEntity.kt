package cloud.poche.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val type: String,
    val createdAt: Long,
    val updatedAt: Long,
    val filePath: String? = null,
    val durationMs: Long? = null,
)
