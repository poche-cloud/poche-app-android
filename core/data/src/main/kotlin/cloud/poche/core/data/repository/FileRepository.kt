package cloud.poche.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface FileRepository {
    suspend fun saveFile(fileName: String, content: ByteArray): Result<File>
    suspend fun getFile(fileName: String): File?
    suspend fun deleteFile(fileName: String): Result<Unit>
}

@Singleton
class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileRepository {

    private val baseDir: File by lazy {
        File(context.filesDir, "attachments").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun saveFile(fileName: String, content: ByteArray): Result<File> = try {
        val file = File(baseDir, fileName)
        file.writeBytes(content)
        Result.success(file)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getFile(fileName: String): File? {
        val file = File(baseDir, fileName)
        return if (file.exists()) file else null
    }

    override suspend fun deleteFile(fileName: String): Result<Unit> = try {
        val file = File(baseDir, fileName)
        if (file.exists() && file.delete()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("File not found or could not be deleted"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
