package tech.deepdrift.metallist.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val densityGCm3: Double,
    val isCustom: Boolean = false,
    val orderIdx: Int = 0,
)
