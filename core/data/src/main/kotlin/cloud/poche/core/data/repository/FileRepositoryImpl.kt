package cloud.poche.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) : FileRepository {

    override fun createTempFile(prefix: String, extension: String): File =
        File.createTempFile(prefix, extension, context.cacheDir)

    override fun saveFile(inputStream: InputStream, fileName: String): Result<String> = try {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        Result.success(file.absolutePath)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getFile(fileName: String): File? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file else null
    }

    override fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
