package tech.deepdrift.metallist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.deepdrift.metallist.domain.calc.MetalCalculator
import tech.deepdrift.metallist.domain.model.CalcDirection
import tech.deepdrift.metallist.domain.model.CalcRequest
import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.domain.model.ShapeMode
import tech.deepdrift.metallist.domain.model.ShapeParams
import kotlin.math.PI

class MetalCalculatorTest {

    @Test fun `круг ⌀20 x 1000мм из стали ~2_465 кг`() {
        // A = π·10² = 314.159 мм² ; V = 314.159 × 1000 = 314159 мм³ = 314.159 см³
        // m = 314.159 × 7.85 / 1000 = 2.4661 кг
        val res = MetalCalculator.calculate(
            CalcRequest(
                shape = ProfileShape.Round,
                params = ShapeParams(d = 20.0),
                densityGCm3 = 7.85,
                direction = CalcDirection.ByLength,
                lengthMm = 1000.0,
                quantity = 1,
            )
        )
        assertEquals(2.4661, res.totalMassKg, 0.001)
        assertEquals(2.4661, res.linearMassKgM, 0.001) // 1 м → та же масса
        assertEquals(PI * 100.0, res.crossSectionAreaMm2, 1e-3)
    }

    @Test fun `режим ГОСТ двутавр №10 из стали, длина 6м → 56_76 кг`() {
        // Табличная погонная масса №10 = 9.46 кг/м
        val res = MetalCalculator.calculate(
            CalcRequest(
                shape = ProfileShape.IBeam,
                mode = ShapeMode.Gost,
                params = ShapeParams(gostNumber = "10"),
                densityGCm3 = 7.85,
                direction = CalcDirection.ByLength,
                lengthMm = 6000.0,
            )
        )
        assertEquals(56.76, res.totalMassKg, 0.01)
        assertEquals(9.46, res.linearMassKgM, 0.001)
    }

    @Test fun `режим ГОСТ арматура №12, из массы 10 кг → длина ~11_26 м`() {
        val res = MetalCalculator.calculate(
            CalcRequest(
                shape = ProfileShape.Rebar,
                mode = ShapeMode.Gost,
                params = ShapeParams(gostNumber = "12"),
                densityGCm3 = 7.85,
                direction = CalcDirection.ByMass,
                massKg = 10.0,
            )
        )
        // 10 / 0.888 = 11.26 м → 11261 мм
        assertEquals(11261.26, res.totalLengthMm, 1.0)
    }

    @Test fun `при плотности алюминия ГОСТ-масса пересчитывается`() {
        val steel = MetalCalculator.calculate(
            CalcRequest(
                shape = ProfileShape.IBeam,
                mode = ShapeMode.Gost,
                params = ShapeParams(gostNumber = "10"),
                densityGCm3 = 7.85,
                direction = CalcDirection.ByLength,
                lengthMm = 1000.0,
            )
        )
        val alu = MetalCalculator.calculate(
            CalcRequest(
                shape = ProfileShape.IBeam,
                mode = ShapeMode.Gost,
                params = ShapeParams(gostNumber = "10"),
                densityGCm3 = 2.70,
                direction = CalcDirection.ByLength,
                lengthMm = 1000.0,
            )
        )
        assertTrue(alu.totalMassKg < steel.totalMassKg)
        val ratio = alu.totalMassKg / steel.totalMassKg
        assertEquals(2.70 / 7.85, ratio, 1e-4)
    }
}
