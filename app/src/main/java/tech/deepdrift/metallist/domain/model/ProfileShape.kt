package tech.deepdrift.metallist.domain.model

/**
 * 11 форм металлопроката.
 * Порядок соответствует главному каталогу (скрин оригинала).
 */
enum class ProfileShape(val supportsGost: Boolean) {
    Round(false),           // Круг сплошной
    PipeRound(false),       // Труба круглая
    Square(false),          // Квадрат сплошной
    Hex(false),             // Шестигранник (размер под ключ)
    Sheet(false),           // Лист / полоса
    BentChannel(false),     // Швеллер гнутый
    PipeRect(false),        // Труба прямоугольная
    Angle(true),            // Уголок равнополочный (ГОСТ 8509)
    IBeam(true),            // Двутавр (ГОСТ 8239)
    Channel(true),          // Швеллер (ГОСТ 8240)
    Rebar(true),            // Арматура (ГОСТ 5781)
}

/** Единицы измерения. */
enum class LengthUnit(val toMm: Double) {
    Mm(1.0),
    Cm(10.0),
    M(1000.0),
}

enum class MassUnit(val toKg: Double) {
    G(0.001),
    Kg(1.0),
    T(1000.0),
}

/** Режим ввода параметров сечения: свободный расчёт или выбор типоразмера ГОСТ. */
enum class ShapeMode { Free, Gost }
