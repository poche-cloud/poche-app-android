package cloud.poche.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkMemo(val id: String, val content: String, val type: String, val createdAt: Long, val updatedAt: Long)
