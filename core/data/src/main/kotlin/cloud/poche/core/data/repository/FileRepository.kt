package cloud.poche.core.data.repository

import java.io.File

interface FileRepository {
    suspend fun saveFile(fileName: String, content: ByteArray): Result<File>
    suspend fun getFile(fileName: String): File?
    suspend fun deleteFile(fileName: String): Result<Unit>
    fun createTempFile(extension: String): File
}
