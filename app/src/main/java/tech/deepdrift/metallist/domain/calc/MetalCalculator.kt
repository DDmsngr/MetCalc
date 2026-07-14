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
        // 1) Профили ГОСТ (двутавр/швеллер/уголок/арматура) — таблица погонной массы.
        if (req.mode == ShapeMode.Gost) {
            val gost = pickGostMass(req)
            if (gost != null) {
                val scale = req.densityGCm3 / GostProfiles.STEEL_DENSITY_G_CM3
                val linear = gost * scale
                val area = linear / (req.densityGCm3 * 1e-3)
                return area to linear
            }
        }

        // 2) Подставные стандарты сечения (ГОСТ 3262 для трубы, ГОСТ 8568/24045 для плиты).
        val standardResult = resolveStandard(req)
        if (standardResult != null) return standardResult

        // 3) Обычный расчёт по геометрии сечения.
        val area = CrossSection.area(req.shape, req.params)
        val linear = area * req.densityGCm3 * 1e-3
        return area to linear
    }

    /**
     * Возвращает (площадь, погонная масса), если запрос использует один из стандартов
     * сечения (ГОСТ 3262 / 8568 / 24045). Иначе null — используется обычная геометрия.
     */
    private fun resolveStandard(req: CalcRequest): Pair<Double, Double>? {
        val std = req.params.standard ?: return null
        val opt = req.params.standardOption
        val ro = req.densityGCm3
        return when (std) {
            "GOST_3262" -> {
                // opt формата "Ду|Класс", напр. "20|Normal"
                val (duStr, clsStr) = opt?.split("|", limit = 2)?.let {
                    (it.getOrNull(0) ?: "") to (it.getOrNull(1) ?: "")
                } ?: return null
                val pipe = duStr.toIntOrNull()?.let(GostPipes3262::byDu) ?: return null
                val cls = runCatching { Pipe3262Class.valueOf(clsStr) }.getOrNull() ?: Pipe3262Class.Normal
                val t = pipe.thickness(cls)
                val area = CrossSection.pipeRound(pipe.dOuter, pipe.dOuter - 2.0 * t)
                area to area * ro * 1e-3
            }
            "GOST_8568" -> {
                // opt: "Rhombus" | "Lentil"
                val pattern = runCatching { RiflePattern.valueOf(opt ?: "") }.getOrNull() ?: return null
                val area = CrossSection.sheet(req.params.t, req.params.b)
                val linear = area * ro * 1e-3 * pattern.coefficient
                // Площадь возвращаем «эффективную», чтобы соответствовать погонной массе
                val effArea = linear / (ro * 1e-3)
                effArea to linear
            }
            "GOST_24045" -> {
                // opt: "Профиль|Толщина", напр. "С21-1000|0.6"
                val (profileName, thickStr) = opt?.split("|", limit = 2)?.let {
                    (it.getOrNull(0) ?: "") to (it.getOrNull(1) ?: "")
                } ?: return null
                val profile = GostProfiledSheets.byName(profileName) ?: return null
                val thick = thickStr.replace(',', '.').toDoubleOrNull() ?: return null
                val row = profile.thicknessOptions.firstOrNull {
                    kotlin.math.abs(it.thicknessMm - thick) < 1e-3
                } ?: return null
                // Табличная масса даётся на м² при ρ=7.85. Пересчёт под материал:
                val massKgM2 = row.massKgM2 * (ro / GostProfiles.STEEL_DENSITY_G_CM3)
                // Погонная масса профлиста при заданной полезной ширине:
                val linear = massKgM2 * (profile.widthMm / 1000.0)
                val area = linear / (ro * 1e-3)
                area to linear
            }
            else -> null
        }
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
