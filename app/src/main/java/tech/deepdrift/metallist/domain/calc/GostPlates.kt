package tech.deepdrift.metallist.domain.calc

/**
 * ГОСТ 8568-77 — лист стальной с ромбическим и чечевичным рифлением.
 *
 * Масса рифлёного листа = масса гладкого × коэффициент утяжеления
 * (учитывает вес выступающих рифов).
 * Коэффициенты — из справочника «Марочник сталей и сплавов» и таблиц
 * ГОСТ 8568-77 (Приложение).
 */
enum class RiflePattern(val label: String, val coefficient: Double) {
    Rhombus("Ромбическое", 1.045),
    Lentil("Чечевичное", 1.055),
}

/**
 * ГОСТ 24045-94 — профилированный листовой прокат (профнастил).
 * Погонная масса дана для стали ρ = 7.85 г/см³.
 * Для других плотностей корректируется через отношение.
 */
data class Profile24045(
    val name: String,      // "С8", "НС35", "Н75"…
    val widthMm: Int,      // полезная ширина листа, мм
    val thicknessOptions: List<ThicknessMass>,
) {
    /** thicknessMm — номинальная толщина стали; massKgM2 — масса на м² профлиста. */
    data class ThicknessMass(val thicknessMm: Double, val massKgM2: Double)
}

object GostProfiledSheets {

    val profiles: List<Profile24045> = listOf(
        Profile24045("С8-1150", 1150, listOf(
            Profile24045.ThicknessMass(0.5, 5.4),
            Profile24045.ThicknessMass(0.55, 5.9),
            Profile24045.ThicknessMass(0.6, 6.5),
            Profile24045.ThicknessMass(0.7, 7.5),
            Profile24045.ThicknessMass(0.8, 8.6),
        )),
        Profile24045("С10-1000", 1000, listOf(
            Profile24045.ThicknessMass(0.5, 5.7),
            Profile24045.ThicknessMass(0.6, 6.8),
            Profile24045.ThicknessMass(0.7, 7.9),
        )),
        Profile24045("С21-1000", 1000, listOf(
            Profile24045.ThicknessMass(0.5, 6.0),
            Profile24045.ThicknessMass(0.6, 7.2),
            Profile24045.ThicknessMass(0.7, 8.4),
            Profile24045.ThicknessMass(0.8, 9.6),
        )),
        Profile24045("НС35-1000", 1000, listOf(
            Profile24045.ThicknessMass(0.55, 6.6),
            Profile24045.ThicknessMass(0.6, 7.2),
            Profile24045.ThicknessMass(0.7, 8.5),
            Profile24045.ThicknessMass(0.8, 9.7),
            Profile24045.ThicknessMass(0.9, 10.9),
        )),
        Profile24045("Н57-750", 750, listOf(
            Profile24045.ThicknessMass(0.6, 7.4),
            Profile24045.ThicknessMass(0.7, 8.7),
            Profile24045.ThicknessMass(0.8, 9.9),
            Profile24045.ThicknessMass(0.9, 11.2),
        )),
        Profile24045("Н60-845", 845, listOf(
            Profile24045.ThicknessMass(0.7, 8.4),
            Profile24045.ThicknessMass(0.8, 9.5),
            Profile24045.ThicknessMass(0.9, 10.7),
        )),
        Profile24045("Н75-750", 750, listOf(
            Profile24045.ThicknessMass(0.8, 10.2),
            Profile24045.ThicknessMass(0.9, 11.5),
            Profile24045.ThicknessMass(1.0, 12.8),
        )),
        Profile24045("Н114-750", 750, listOf(
            Profile24045.ThicknessMass(0.8, 11.1),
            Profile24045.ThicknessMass(0.9, 12.5),
            Profile24045.ThicknessMass(1.0, 13.9),
        )),
    )

    fun byName(name: String): Profile24045? = profiles.firstOrNull { it.name == name }
}
