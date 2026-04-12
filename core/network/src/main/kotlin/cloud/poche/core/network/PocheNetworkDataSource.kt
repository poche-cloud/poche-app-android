package cloud.poche.core.network

import cloud.poche.core.network.model.NetworkBookmark
import cloud.poche.core.network.model.NetworkMemo

interface PocheNetworkDataSource {
    suspend fun getBookmarks(): List<NetworkBookmark>
    suspend fun createBookmark(url: String, collectionId: String?): NetworkBookmark
    suspend fun updateBookmark(id: String, title: String?, description: String?): NetworkBookmark
    suspend fun deleteBookmark(id: String)

    suspend fun getMemos(): List<NetworkMemo>
    suspend fun createMemo(title: String, content: String, collectionId: String?): NetworkMemo
    suspend fun updateMemo(id: String, title: String?, content: String?): NetworkMemo
    suspend fun deleteMemo(id: String)

    suspend fun search(query: String): List<NetworkSearchResult>
}

data class NetworkSearchResult(val id: String, val type: String, val title: String, val score: Double)
