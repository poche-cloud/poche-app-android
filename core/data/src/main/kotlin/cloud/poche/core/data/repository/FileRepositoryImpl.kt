package cloud.poche.core.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) : FileRepository {

    private val filesDir = context.filesDir

    override suspend fun saveFile(uri: Uri, targetName: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val targetFile = File(filesDir, targetName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: error("Could not open input stream for URI: $uri")
            targetFile
        }
    }

    override suspend fun saveFile(source: File, targetName: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val targetFile = File(filesDir, targetName)
            source.copyTo(targetFile, overwrite = true)
            targetFile
        }
    }

    override suspend fun getFile(name: String): File? {
        val file = File(filesDir, name)
        return if (file.exists()) file else null
    }

    override suspend fun deleteFile(name: String): Boolean {
        val file = File(filesDir, name)
        return if (file.exists()) file.delete() else false
    }

    override suspend fun createTempFile(prefix: String, suffix: String): File =
        File.createTempFile(prefix, suffix, context.cacheDir)
}
