package tech.deepdrift.metallist.data.repository

import kotlinx.coroutines.flow.Flow
import tech.deepdrift.metallist.data.db.HistoryDao
import tech.deepdrift.metallist.data.db.HistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: HistoryDao,
) {
    fun observeAll(): Flow<List<HistoryEntity>> = dao.observeAll()

    suspend fun add(item: HistoryEntity): Long = dao.insert(item)

    suspend fun remove(item: HistoryEntity) = dao.delete(item)

    suspend fun clearAll() = dao.clearAll()

    suspend fun getById(id: Long): HistoryEntity? = dao.getById(id)
}
