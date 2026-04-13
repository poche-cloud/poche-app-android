package cloud.poche.core.data.repository

import android.net.Uri
import java.io.File

interface FileRepository {
    suspend fun saveFile(uri: Uri, targetName: String): Result<File>
    suspend fun saveFile(source: File, targetName: String): Result<File>
    suspend fun getFile(name: String): File?
    suspend fun deleteFile(name: String): Boolean
    suspend fun createTempFile(prefix: String, suffix: String): File
}
