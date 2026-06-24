package cloud.poche.core.network

import cloud.poche.core.network.model.NetworkBookmark
import cloud.poche.core.network.model.NetworkMemo
import cloud.poche.core.network.model.NetworkSearchResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorPocheNetwork @Inject constructor(private val client: HttpClient) : PocheNetworkDataSource {

    private val baseUrl = "https://api.poche.cloud/v1" // Placeholder

    override suspend fun getBookmarks(): List<NetworkBookmark> = client.get("$baseUrl/bookmarks").body()

    override suspend fun createBookmark(url: String, collectionId: String?): NetworkBookmark =
        client.post("$baseUrl/bookmarks") {
            setBody(CreateBookmarkRequest(url, collectionId))
        }.body()

    override suspend fun updateBookmark(id: String, title: String?, description: String?): NetworkBookmark =
        client.put("$baseUrl/bookmarks/$id") {
            setBody(UpdateBookmarkRequest(title, description))
        }.body()

    override suspend fun deleteBookmark(id: String) {
        client.delete("$baseUrl/bookmarks/$id")
    }

    override suspend fun getMemos(): List<NetworkMemo> = client.get("$baseUrl/memos").body()

    override suspend fun createMemo(title: String, content: String, collectionId: String?): NetworkMemo =
        client.post("$baseUrl/memos") {
            setBody(CreateMemoRequest(title, content, collectionId))
        }.body()

    override suspend fun updateMemo(id: String, title: String?, content: String?): NetworkMemo =
        client.put("$baseUrl/memos/$id") {
            setBody(UpdateMemoRequest(title, content))
        }.body()

    override suspend fun deleteMemo(id: String) {
        client.delete("$baseUrl/memos/$id")
    }

    override suspend fun search(query: String): List<NetworkSearchResult> = client.post("$baseUrl/search") {
        setBody(SearchRequest(query))
    }.body<SearchResponse>().results
}

@Serializable
private data class CreateBookmarkRequest(val url: String, val collectionId: String?)

@Serializable
private data class UpdateBookmarkRequest(val title: String?, val description: String?)

@Serializable
private data class CreateMemoRequest(val title: String, val content: String, val collectionId: String?)

@Serializable
private data class UpdateMemoRequest(val title: String?, val content: String?)

@Serializable
private data class SearchRequest(val query: String)

@Serializable
private data class SearchResponse(val results: List<NetworkSearchResult>)
