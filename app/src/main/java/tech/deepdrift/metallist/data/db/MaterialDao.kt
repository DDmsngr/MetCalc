package tech.deepdrift.metallist.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {

    @Query("SELECT * FROM materials ORDER BY orderIdx ASC, name ASC")
    fun observeAll(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MaterialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MaterialEntity>)

    @Update
    suspend fun update(entity: MaterialEntity)

    @Delete
    suspend fun delete(entity: MaterialEntity)

    @Query("SELECT COUNT(*) FROM materials")
    suspend fun count(): Int
}
