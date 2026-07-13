package tech.deepdrift.metallist.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Запись истории. Универсальная — используется как для расчётов проката,
 * так и для расчётов допусков ISO.
 *
 * kind: "metal" | "iso"
 * payload: JSON запроса, необходимого для точного повтора расчёта.
 * summary / value1..3: короткие поля для быстрого отображения без парсинга JSON.
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val kind: String,
    val title: String,
    val summary: String,
    val payloadJson: String,
)
