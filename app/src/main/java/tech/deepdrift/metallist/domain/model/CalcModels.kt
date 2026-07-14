package tech.deepdrift.metallist.domain.model

/**
 * Направление расчёта:
 * - ByLength: пользователь задаёт длину → считаем массу
 * - ByMass: пользователь задаёт массу → считаем длину
 */
enum class CalcDirection { ByLength, ByMass }

/**
 * Параметры сечения. Значения в мм.
 * Для каждой формы используются свои поля — остальные игнорируются.
 */
data class ShapeParams(
    val d: Double = 0.0,        // диаметр внешний / сторона
    val d2: Double = 0.0,       // внутренний диаметр / внутр. размеры
    val h: Double = 0.0,        // высота
    val b: Double = 0.0,        // ширина / ширина полки
    val t: Double = 0.0,        // толщина / толщина полки
    val s: Double = 0.0,        // толщина стенки
    val gostNumber: String? = null,     // для ГОСТ-режима (двутавр/швеллер/уголок/арматура)
    val standard: String? = null,       // выбранный стандарт: "GOST_3262" / "GOST_8568" / "GOST_24045"
    val standardOption: String? = null, // опция стандарта: "20|Normal", "Rhombus", "С21-1000|0.6"
)

data class CalcRequest(
    val shape: ProfileShape,
    val mode: ShapeMode = ShapeMode.Free,
    val params: ShapeParams,
    val densityGCm3: Double,
    val direction: CalcDirection,
    val lengthMm: Double = 0.0,
    val massKg: Double = 0.0,
    val quantity: Int = 1,
    val pricePerKg: Double = 0.0,
)

data class CalcResult(
    val volumeMm3: Double,      // объём одной единицы, мм³
    val singleMassKg: Double,   // масса одной единицы
    val totalMassKg: Double,    // масса всех штук (× количество)
    val totalLengthMm: Double,  // суммарная длина (для режима по массе — вычисляется)
    val linearMassKgM: Double,  // погонная масса кг/м
    val totalPrice: Double,     // цена за весь объём
    val crossSectionAreaMm2: Double, // площадь сечения
)
