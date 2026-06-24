package cloud.poche.core.data.repository

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FileRepositoryTest {

    class FakeFileRepository(private val tempDir: File) : FileRepository {
        override suspend fun saveFile(uri: Uri, targetName: String): Result<File> {
            val file = File(tempDir, targetName)
            file.writeText("fake content from uri")
            return Result.success(file)
        }

        override suspend fun saveFile(source: File, targetName: String): Result<File> {
            val file = File(tempDir, targetName)
            source.copyTo(file, overwrite = true)
            return Result.success(file)
        }

        override suspend fun getFile(name: String): Result<File> {
            val file = File(tempDir, name)
            return if (file.exists()) Result.success(file) else Result.failure(Exception("Not found"))
        }

        override suspend fun deleteFile(name: String): Result<Unit> {
            val file = File(tempDir, name)
            return if (file.exists() &&
                file.delete()
            ) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed"))
            }
        }

        override suspend fun createTempFile(prefix: String, suffix: String): File =
            File.createTempFile(prefix, suffix, tempDir)
    }

    @Test
    fun `test saveFile, getFile and deleteFile with Fake`(@TempDir tempDir: File) = runTest {
        val repository = FakeFileRepository(tempDir)
        val sourceFile = File(tempDir, "source.txt")
        sourceFile.writeText("test content")

        val saveResult = repository.saveFile(sourceFile, "test.txt")
        assertTrue(saveResult.isSuccess)

        val getResult = repository.getFile("test.txt")
        assertTrue(getResult.isSuccess)
        assertEquals("test content", getResult.getOrNull()?.readText())

        val deleteResult = repository.deleteFile("test.txt")
        assertTrue(deleteResult.isSuccess)

        val getResultAfterDelete = repository.getFile("test.txt")
        assertTrue(getResultAfterDelete.isFailure)
    }
}
