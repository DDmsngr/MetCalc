package tech.deepdrift.metallist.domain.calc

/**
 * Справочник типоразмеров ГОСТ для проката, у которого форма определяется номером.
 *
 * Значения — из ГОСТ 8239-89 (двутавры), ГОСТ 8240-97 (швеллеры),
 * ГОСТ 8509-93 (уголки равнополочные), ГОСТ 5781-82 (арматура горячекатаная).
 *
 * Погонная масса приведена для стали ρ = 7.85 г/см³.
 * Для других материалов масса пересчитывается через отношение плотностей.
 */

/** Двутавр по ГОСТ 8239: h, b, s (стенка), t (полка) — мм, mass — кг/м. */
data class GostIBeam(
    val number: String,
    val h: Double,
    val b: Double,
    val s: Double,
    val t: Double,
    val massKgM: Double,
)

/** Швеллер по ГОСТ 8240: h, b, s, t — мм, mass — кг/м. */
data class GostChannel(
    val number: String,
    val h: Double,
    val b: Double,
    val s: Double,
    val t: Double,
    val massKgM: Double,
)

/** Уголок равнополочный по ГОСТ 8509: b — полка, t — толщина, mass — кг/м. */
data class GostAngle(
    val number: String,
    val b: Double,
    val t: Double,
    val massKgM: Double,
)

/** Арматура по ГОСТ 5781: d — номинальный диаметр, mass — кг/м. */
data class GostRebar(
    val number: String,
    val d: Double,
    val massKgM: Double,
)

object GostProfiles {

    val iBeams: List<GostIBeam> = listOf(
        GostIBeam("10", 100.0, 55.0, 4.5, 7.2, 9.46),
        GostIBeam("12", 120.0, 64.0, 4.8, 7.3, 11.5),
        GostIBeam("14", 140.0, 73.0, 4.9, 7.5, 13.7),
        GostIBeam("16", 160.0, 81.0, 5.0, 7.8, 15.9),
        GostIBeam("18", 180.0, 90.0, 5.1, 8.1, 18.4),
        GostIBeam("20", 200.0, 100.0, 5.2, 8.4, 21.0),
        GostIBeam("22", 220.0, 110.0, 5.4, 8.7, 24.0),
        GostIBeam("24", 240.0, 115.0, 5.6, 9.5, 27.3),
        GostIBeam("27", 270.0, 125.0, 6.0, 9.8, 31.5),
        GostIBeam("30", 300.0, 135.0, 6.5, 10.2, 36.5),
        GostIBeam("33", 330.0, 140.0, 7.0, 11.2, 42.2),
        GostIBeam("36", 360.0, 145.0, 7.5, 12.3, 48.6),
        GostIBeam("40", 400.0, 155.0, 8.3, 13.0, 57.0),
        GostIBeam("45", 450.0, 160.0, 9.0, 14.2, 66.5),
        GostIBeam("50", 500.0, 170.0, 10.0, 15.2, 78.5),
        GostIBeam("55", 550.0, 180.0, 11.0, 16.5, 92.6),
        GostIBeam("60", 600.0, 190.0, 12.0, 17.8, 108.0),
    )

    val channels: List<GostChannel> = listOf(
        GostChannel("5", 50.0, 32.0, 4.4, 7.0, 4.84),
        GostChannel("6.5", 65.0, 36.0, 4.4, 7.2, 5.90),
        GostChannel("8", 80.0, 40.0, 4.5, 7.4, 7.05),
        GostChannel("10", 100.0, 46.0, 4.5, 7.6, 8.59),
        GostChannel("12", 120.0, 52.0, 4.8, 7.8, 10.4),
        GostChannel("14", 140.0, 58.0, 4.9, 8.1, 12.3),
        GostChannel("16", 160.0, 64.0, 5.0, 8.4, 14.2),
        GostChannel("18", 180.0, 70.0, 5.1, 8.7, 16.3),
        GostChannel("20", 200.0, 76.0, 5.2, 9.0, 18.4),
        GostChannel("22", 220.0, 82.0, 5.4, 9.5, 21.0),
        GostChannel("24", 240.0, 90.0, 5.6, 10.0, 24.0),
        GostChannel("27", 270.0, 95.0, 6.0, 10.5, 27.7),
        GostChannel("30", 300.0, 100.0, 6.5, 11.0, 31.8),
        GostChannel("33", 330.0, 105.0, 7.0, 11.7, 36.5),
        GostChannel("36", 360.0, 110.0, 7.5, 12.6, 41.9),
        GostChannel("40", 400.0, 115.0, 8.0, 13.5, 48.3),
    )

    val angles: List<GostAngle> = listOf(
        GostAngle("20×3", 20.0, 3.0, 0.89),
        GostAngle("25×3", 25.0, 3.0, 1.12),
        GostAngle("25×4", 25.0, 4.0, 1.46),
        GostAngle("30×3", 30.0, 3.0, 1.36),
        GostAngle("30×4", 30.0, 4.0, 1.78),
        GostAngle("35×4", 35.0, 4.0, 2.10),
        GostAngle("40×4", 40.0, 4.0, 2.42),
        GostAngle("40×5", 40.0, 5.0, 2.98),
        GostAngle("45×4", 45.0, 4.0, 2.73),
        GostAngle("45×5", 45.0, 5.0, 3.37),
        GostAngle("50×4", 50.0, 4.0, 3.05),
        GostAngle("50×5", 50.0, 5.0, 3.77),
        GostAngle("50×6", 50.0, 6.0, 4.47),
        GostAngle("63×5", 63.0, 5.0, 4.81),
        GostAngle("63×6", 63.0, 6.0, 5.72),
        GostAngle("70×5", 70.0, 5.0, 5.38),
        GostAngle("75×5", 75.0, 5.0, 5.80),
        GostAngle("75×6", 75.0, 6.0, 6.89),
        GostAngle("75×7", 75.0, 7.0, 7.96),
        GostAngle("75×8", 75.0, 8.0, 9.02),
        GostAngle("80×6", 80.0, 6.0, 7.36),
        GostAngle("80×8", 80.0, 8.0, 9.65),
        GostAngle("90×6", 90.0, 6.0, 8.33),
        GostAngle("90×7", 90.0, 7.0, 9.64),
        GostAngle("90×8", 90.0, 8.0, 10.93),
        GostAngle("100×7", 100.0, 7.0, 10.79),
        GostAngle("100×8", 100.0, 8.0, 12.25),
        GostAngle("100×10", 100.0, 10.0, 15.10),
        GostAngle("100×12", 100.0, 12.0, 17.90),
        GostAngle("125×8", 125.0, 8.0, 15.46),
        GostAngle("125×10", 125.0, 10.0, 19.10),
        GostAngle("140×10", 140.0, 10.0, 21.45),
        GostAngle("160×10", 160.0, 10.0, 24.67),
        GostAngle("160×12", 160.0, 12.0, 29.35),
        GostAngle("200×12", 200.0, 12.0, 36.97),
        GostAngle("200×16", 200.0, 16.0, 48.65),
    )

    /** Арматура горячекатаная по ГОСТ 5781, погонная масса стали ρ=7.85. */
    val rebars: List<GostRebar> = listOf(
        GostRebar("6", 6.0, 0.222),
        GostRebar("8", 8.0, 0.395),
        GostRebar("10", 10.0, 0.617),
        GostRebar("12", 12.0, 0.888),
        GostRebar("14", 14.0, 1.208),
        GostRebar("16", 16.0, 1.578),
        GostRebar("18", 18.0, 1.998),
        GostRebar("20", 20.0, 2.466),
        GostRebar("22", 22.0, 2.984),
        GostRebar("25", 25.0, 3.850),
        GostRebar("28", 28.0, 4.834),
        GostRebar("32", 32.0, 6.313),
        GostRebar("36", 36.0, 7.990),
        GostRebar("40", 40.0, 9.865),
        GostRebar("45", 45.0, 12.480),
        GostRebar("50", 50.0, 15.410),
        GostRebar("55", 55.0, 18.650),
        GostRebar("60", 60.0, 22.190),
        GostRebar("70", 70.0, 30.210),
        GostRebar("80", 80.0, 39.460),
    )

    /** Базовая плотность стали, к которой привязаны табличные значения ГОСТ. */
    const val STEEL_DENSITY_G_CM3 = 7.85
}
