package tech.deepdrift.metallist.domain.calc

/**
 * Сортамент ГОСТ 3262-75 — трубы стальные водогазопроводные (ВГП).
 * Три класса по толщине стенки: лёгкая, обычная, усиленная.
 *
 * Данные из ГОСТ 3262-75 Таблица 1.
 */
enum class Pipe3262Class(val label: String) {
    Light("Лёгкая"),
    Normal("Обычная"),
    Heavy("Усиленная"),
}

data class Pipe3262(
    val du: Int,           // условный проход, мм
    val dOuter: Double,    // наружный диаметр, мм
    val tLight: Double,    // толщина стенки лёгкой, мм
    val tNormal: Double,   // толщина стенки обычной, мм
    val tHeavy: Double,    // толщина стенки усиленной, мм
) {
    fun thickness(cls: Pipe3262Class): Double = when (cls) {
        Pipe3262Class.Light -> tLight
        Pipe3262Class.Normal -> tNormal
        Pipe3262Class.Heavy -> tHeavy
    }
    val label: String get() = "Ду $du (⌀$dOuter)"
}

object GostPipes3262 {

    val all: List<Pipe3262> = listOf(
        Pipe3262(6,   10.2, 1.8, 2.0, 2.5),
        Pipe3262(8,   13.5, 2.0, 2.2, 2.8),
        Pipe3262(10,  17.0, 2.0, 2.2, 2.8),
        Pipe3262(15,  21.3, 2.5, 2.8, 3.2),
        Pipe3262(20,  26.8, 2.5, 2.8, 3.2),
        Pipe3262(25,  33.5, 2.8, 3.2, 4.0),
        Pipe3262(32,  42.3, 2.8, 3.2, 4.0),
        Pipe3262(40,  48.0, 3.0, 3.5, 4.0),
        Pipe3262(50,  60.0, 3.0, 3.5, 4.5),
        Pipe3262(65,  75.5, 3.2, 4.0, 4.5),
        Pipe3262(80,  88.5, 3.5, 4.0, 4.5),
        Pipe3262(90, 101.3, 3.5, 4.0, 4.5),
        Pipe3262(100, 114.0, 4.0, 4.5, 5.0),
        Pipe3262(125, 140.0, 4.0, 4.5, 5.5),
        Pipe3262(150, 165.0, 4.0, 4.5, 5.5),
    )

    fun byDu(du: Int): Pipe3262? = all.firstOrNull { it.du == du }
}
