package cloud.poche.core.domain.usecase

import android.content.Context
import cloud.poche.core.model.StorageUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class GetStorageUsageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(): StorageUsage {
        val dbFile = context.getDatabasePath("poche-database")
        val databaseSize = if (dbFile.exists()) dbFile.length() else 0L

        val capturesDir = File(context.filesDir, "captures")
        val filesSize = if (capturesDir.exists()) {
            capturesDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        } else {
            0L
        }

        return StorageUsage(
            databaseSizeBytes = databaseSize,
            filesSizeBytes = filesSize,
        )
    }
}
