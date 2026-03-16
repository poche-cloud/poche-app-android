package cloud.poche.core.model

data class StorageUsage(val databaseSizeBytes: Long, val filesSizeBytes: Long) {
    val totalSizeBytes: Long get() = databaseSizeBytes + filesSizeBytes
}
