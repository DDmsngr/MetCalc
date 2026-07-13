package tech.deepdrift.metallist.domain.calc

import tech.deepdrift.metallist.domain.model.CalcDirection
import tech.deepdrift.metallist.domain.model.CalcRequest
import tech.deepdrift.metallist.domain.model.CalcResult
import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.domain.model.ShapeMode
import kotlin.math.max

/**
 * Основной калькулятор массы/длины проката.
 *
 * Плотность в г/см³; длины в мм; массы в кг.
 * Единицы конвертируются на уровне UI (LengthUnit / MassUnit).
 */
object MetalCalculator {

    fun calculate(req: CalcRequest): CalcResult {
        val (area, linearMassKgM) = areaAndLinearMass(req)

        return when (req.direction) {
            CalcDirection.ByLength -> {
                val singleLenMm = max(0.0, req.lengthMm)
                val volumeMm3 = area * singleLenMm
                val singleMass = linearMassKgM * (singleLenMm / 1000.0)
                val totalMass = singleMass * req.quantity
                CalcResult(
                    volumeMm3 = volumeMm3,
                    singleMassKg = singleMass,
                    totalMassKg = totalMass,
                    totalLengthMm = singleLenMm * req.quantity,
                    linearMassKgM = linearMassKgM,
                    totalPrice = totalMass * req.pricePerKg,
                    crossSectionAreaMm2 = area,
                )
            }
            CalcDirection.ByMass -> {
                val totalMass = max(0.0, req.massKg)
                val totalLenMm = if (linearMassKgM > 0.0) {
                    (totalMass / linearMassKgM) * 1000.0
                } else 0.0
                val singleLenMm = if (req.quantity > 0) totalLenMm / req.quantity else 0.0
                val volumeMm3 = area * singleLenMm
                CalcResult(
                    volumeMm3 = volumeMm3,
                    singleMassKg = if (req.quantity > 0) totalMass / req.quantity else totalMass,
                    totalMassKg = totalMass,
                    totalLengthMm = totalLenMm,
                    linearMassKgM = linearMassKgM,
                    totalPrice = totalMass * req.pricePerKg,
                    crossSectionAreaMm2 = area,
                )
            }
        }
    }

    /**
     * Возвращает пару (площадь сечения мм², погонная масса кг/м) для заданного запроса.
     * В режиме ГОСТ погонная масса берётся из справочника и пересчитывается под плотность,
     * если материал отличается от стали.
     */
    private fun areaAndLinearMass(req: CalcRequest): Pair<Double, Double> {
        if (req.mode == ShapeMode.Gost) {
            val gost = pickGostMass(req)
            if (gost != null) {
                // Пересчёт под пользовательскую плотность
                val scale = req.densityGCm3 / GostProfiles.STEEL_DENSITY_G_CM3
                val linear = gost * scale
                // площадь считаем обратно из погонной массы (для консистентности отображения):
                // m [кг/м] = A [мм²] * ρ [г/см³] * 1e-3
                // A [мм²] = m / (ρ * 1e-3)
                val area = linear / (req.densityGCm3 * 1e-3)
                return area to linear
            }
        }
        val area = CrossSection.area(req.shape, req.params)
        // Погонная масса: A [мм²] × ρ [г/см³] × 1e-3 = кг/м
        val linear = area * req.densityGCm3 * 1e-3
        return area to linear
    }

    private fun pickGostMass(req: CalcRequest): Double? {
        val num = req.params.gostNumber ?: return null
        return when (req.shape) {
            ProfileShape.IBeam -> GostProfiles.iBeams.firstOrNull { it.number == num }?.massKgM
            ProfileShape.Channel -> GostProfiles.channels.firstOrNull { it.number == num }?.massKgM
            ProfileShape.Angle -> GostProfiles.angles.firstOrNull { it.number == num }?.massKgM
            ProfileShape.Rebar -> GostProfiles.rebars.firstOrNull { it.number == num }?.massKgM
            else -> null
        }
    }
}
