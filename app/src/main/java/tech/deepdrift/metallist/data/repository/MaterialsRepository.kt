package tech.deepdrift.metallist.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.deepdrift.metallist.data.db.MaterialDao
import tech.deepdrift.metallist.data.db.MaterialEntity
import tech.deepdrift.metallist.domain.model.Material
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialsRepository @Inject constructor(
    private val dao: MaterialDao,
) {
    fun observeAll(): Flow<List<Material>> =
        dao.observeAll().map { list -> list.map(::toDomain) }

    suspend fun add(name: String, density: Double) {
        dao.insert(MaterialEntity(name = name, densityGCm3 = density, isCustom = true, orderIdx = 999))
    }

    suspend fun update(material: Material) {
        dao.update(MaterialEntity(id = material.id, name = material.name, densityGCm3 = material.densityGCm3, isCustom = material.isCustom))
    }

    suspend fun remove(material: Material) {
        dao.delete(MaterialEntity(id = material.id, name = material.name, densityGCm3 = material.densityGCm3, isCustom = material.isCustom))
    }

    private fun toDomain(e: MaterialEntity) = Material(
        id = e.id,
        name = e.name,
        densityGCm3 = e.densityGCm3,
        isCustom = e.isCustom,
    )
}
