package tech.deepdrift.metallist

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.deepdrift.metallist.domain.calc.CrossSection
import kotlin.math.PI

class CrossSectionTest {

    private val eps = 1e-6

    @Test fun `круг диаметр 20 мм = π·100`() {
        assertEquals(PI * 100.0, CrossSection.round(20.0), eps)
    }

    @Test fun `труба круглая 20x16 внутренний`() {
        val a = CrossSection.pipeRound(20.0, 16.0)
        assertEquals(PI * (400.0 - 256.0) / 4.0, a, eps)
    }

    @Test fun `квадрат сторона 10 = 100`() {
        assertEquals(100.0, CrossSection.square(10.0), eps)
    }

    @Test fun `шестигранник размер под ключ 10`() {
        val expected = (Math.sqrt(3.0) / 2.0) * 100.0
        assertEquals(expected, CrossSection.hex(10.0), eps)
    }

    @Test fun `лист 3x100 = 300`() {
        assertEquals(300.0, CrossSection.sheet(3.0, 100.0), eps)
    }

    @Test fun `труба прямоугольная 40x20 t=2`() {
        val outer = 40.0 * 20.0
        val inner = (40.0 - 4.0) * (20.0 - 4.0)
        assertEquals(outer - inner, CrossSection.pipeRect(40.0, 20.0, 2.0), eps)
    }

    @Test fun `уголок 50x5 равнополочный`() {
        val expected = 5.0 * (2.0 * 50.0 - 5.0)
        assertEquals(expected, CrossSection.angleEqual(50.0, 5.0), eps)
    }

    @Test fun `двутавр упрощённо`() {
        val h = 100.0; val b = 55.0; val s = 4.5; val t = 7.2
        val expected = 2.0 * b * t + (h - 2.0 * t) * s
        assertEquals(expected, CrossSection.iBeam(h, b, s, t), eps)
    }
}
