package cloud.poche.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkBookmark(
    val id: String,
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val ogImageUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
