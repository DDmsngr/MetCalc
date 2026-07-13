package tech.deepdrift.metallist.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.deepdrift.metallist.data.db.HistoryDao
import tech.deepdrift.metallist.data.db.MaterialDao
import tech.deepdrift.metallist.data.db.MetallistDatabase
import tech.deepdrift.metallist.data.seed.MaterialsSeed
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): MetallistDatabase {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        lateinit var db: MetallistDatabase
        db = Room.databaseBuilder(ctx, MetallistDatabase::class.java, "metallist.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(sqlite: SupportSQLiteDatabase) {
                    scope.launch {
                        val dao = db.materialDao()
                        if (dao.count() == 0) dao.insertAll(MaterialsSeed.defaults)
                    }
                }
            })
            .build()
        return db
    }

    @Provides fun provideMaterialDao(db: MetallistDatabase): MaterialDao = db.materialDao()

    @Provides fun provideHistoryDao(db: MetallistDatabase): HistoryDao = db.historyDao()
}
