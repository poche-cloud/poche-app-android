package cloud.poche.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileRepository {

    override fun createTempFile(extension: String): File {
        return File.createTempFile("temp", ".$extension", context.cacheDir)
    }

    override suspend fun saveFile(fileName: String, content: ByteArray): Result<File> = runCatching {
        val file = File(context.filesDir, fileName)
        file.writeBytes(content)
        file
    }

    override suspend fun getFile(fileName: String): File? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file else null
    }

    override suspend fun deleteFile(fileName: String): Result<Unit> = runCatching {
        val file = File(context.filesDir, fileName)
        if (file.exists() && !file.delete()) {
            throw Exception("Failed to delete file")
        }
    }
}
