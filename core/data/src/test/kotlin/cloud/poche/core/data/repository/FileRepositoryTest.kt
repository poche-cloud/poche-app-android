package cloud.poche.core.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class FileRepositoryTest {

    private class FakeFileRepository : FileRepository {
        private val files = mutableMapOf<String, ByteArray>()
        private val tempDir = File("build/tmp/test").apply { mkdirs() }

        override suspend fun saveFile(fileName: String, content: ByteArray): Result<File> {
            files[fileName] = content
            val file = File(tempDir, fileName)
            file.writeBytes(content)
            return Result.success(file)
        }

        override suspend fun getFile(fileName: String): File? {
            return if (files.containsKey(fileName)) File(tempDir, fileName) else null
        }

        override suspend fun deleteFile(fileName: String): Result<Unit> {
            files.remove(fileName)
            return Result.success(Unit)
        }
    }

    @Test
    fun `save and get file works`() = runTest {
        val repo = FakeFileRepository()
        val content = "hello world".toByteArray()
        
        repo.saveFile("test.txt", content)
        val file = repo.getFile("test.txt")
        
        assertTrue(file != null)
        assertEquals("hello world", file?.readText())
    }

    @Test
    fun `delete file removes it`() = runTest {
        val repo = FakeFileRepository()
        repo.saveFile("delete.txt", "data".toByteArray())
        repo.deleteFile("delete.txt")
        
        assertNull(repo.getFile("delete.txt"))
    }
}
