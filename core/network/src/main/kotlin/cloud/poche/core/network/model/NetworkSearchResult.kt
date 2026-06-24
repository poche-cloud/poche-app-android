package cloud.poche.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkSearchResult(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    val score: Double? = null,
)
