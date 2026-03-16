package cloud.poche.core.database.di

import android.content.Context
import androidx.room.Room
import cloud.poche.core.database.PocheDatabase
import cloud.poche.core.database.dao.MemoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PocheDatabase = Room.databaseBuilder(
        context,
        PocheDatabase::class.java,
        "poche-database",
    ).build()

    @Provides
    fun provideMemoDao(database: PocheDatabase): MemoDao = database.memoDao()
}
