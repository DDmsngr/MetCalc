package tech.deepdrift.metallist.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MaterialEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MetallistDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun historyDao(): HistoryDao
}
