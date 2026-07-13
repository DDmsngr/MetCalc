package tech.deepdrift.metallist.domain.iso

/**
 * Расчёт результата допуска ISO 286 для комбинации «размер × буква × квалитет».
 */
object IsoCalculator {

    fun compute(sizeMm: Double, letter: String, grade: String): IsoResult? {
        val dev = IsoDeviations.deviations(letter, sizeMm, grade) ?: return null
        val toleranceMicrons = dev.es - dev.ei
        val upperMm = sizeMm + dev.es / 1000.0
        val lowerMm = sizeMm + dev.ei / 1000.0
        val avgMm = (upperMm + lowerMm) / 2.0
        val isHole = letter.first().isUpperCase() || letter == "JS"
        return IsoResult(
            sizeMm = sizeMm,
            letter = letter,
            grade = grade,
            isHole = isHole,
            esMicrons = dev.es,
            eiMicrons = dev.ei,
            toleranceMicrons = toleranceMicrons,
            upperMm = upperMm,
            lowerMm = lowerMm,
            avgMm = avgMm,
        )
    }
}

data class IsoResult(
    val sizeMm: Double,
    val letter: String,
    val grade: String,
    val isHole: Boolean,
    val esMicrons: Double,
    val eiMicrons: Double,
    val toleranceMicrons: Double,
    val upperMm: Double,
    val lowerMm: Double,
    val avgMm: Double,
)
