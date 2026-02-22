package cloud.poche.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cloud.poche.core.database.dao.MemoDao
import cloud.poche.core.database.entity.MemoEntity

@Database(
    entities = [MemoEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PocheDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
}
