package cloud.poche.core.data.repository

import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File

class FileRepositoryTest {

    private lateinit var fileRepository: FileRepository
    private lateinit var context: Context

    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        every { context.filesDir } returns tempDir
        every { context.cacheDir } returns tempDir

        fileRepository = FileRepositoryImpl(context)
    }

    @Test
    fun `saveFile from source file works`() = runTest {
        val sourceFile = File(tempDir, "source.txt")
        sourceFile.writeText("test content")

        val result = fileRepository.saveFile(sourceFile, "target.txt")

        assertTrue(result.isSuccess)
        val targetFile = result.getOrThrow()
        assertEquals("test content", targetFile.readText())
    }

    @Test
    fun `getFile returns file if exists`() = runTest {
        val file = File(tempDir, "existing.txt")
        file.writeText("hello")

        val result = fileRepository.getFile("existing.txt")

        assertNotNull(result)
        assertEquals("hello", result?.readText())
    }

    @Test
    fun `getFile returns null if not exists`() = runTest {
        val result = fileRepository.getFile("non_existent.txt")
        assertNull(result)
    }

    @Test
    fun `deleteFile works`() = runTest {
        val file = File(tempDir, "to_delete.txt")
        file.writeText("bye")

        val result = fileRepository.deleteFile("to_delete.txt")

        assertTrue(result)
        assertFalse(File(tempDir, "to_delete.txt").exists())
    }
}
