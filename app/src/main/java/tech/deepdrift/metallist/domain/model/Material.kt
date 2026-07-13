package tech.deepdrift.metallist.domain.model

/**
 * Материал с плотностью (г/см³) и признаком встроенный/пользовательский.
 */
data class Material(
    val id: Long,
    val name: String,
    val densityGCm3: Double,
    val isCustom: Boolean,
)
