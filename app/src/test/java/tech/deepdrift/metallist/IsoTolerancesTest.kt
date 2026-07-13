package tech.deepdrift.metallist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import tech.deepdrift.metallist.domain.iso.IsoCalculator
import tech.deepdrift.metallist.domain.iso.IsoTolerances

class IsoTolerancesTest {

    /** Проверка ключевых значений из ISO 286-1 Table 1. */
    @Test fun `IT7 для 0-3 = 10 мкм`() {
        assertEquals(10.0, IsoTolerances.itMicrons(2.0, "IT7"))
    }

    @Test fun `IT6 для 18-30 = 13 мкм`() {
        assertEquals(13.0, IsoTolerances.itMicrons(25.0, "IT6"))
    }

    @Test fun `IT11 для 80-120 = 220 мкм`() {
        assertEquals(220.0, IsoTolerances.itMicrons(100.0, "IT11"))
    }

    @Test fun `IT01 для 400-500 = 4 мкм`() {
        assertEquals(4.0, IsoTolerances.itMicrons(450.0, "IT01"))
    }

    /**
     * H7 для 25 мм — классический пример: EI=0, ES=+21 мкм.
     * (25 мм в интервале 18-30; IT7 = 21).
     */
    @Test fun `H7 25мм = 0 плюс21`() {
        val r = IsoCalculator.compute(25.0, "H", "IT7"); assertNotNull(r)
        r!!
        assertEquals(21.0, r.esMicrons, 1e-6)
        assertEquals(0.0, r.eiMicrons, 1e-6)
    }

    /**
     * h7 для 25 мм: es=0, ei=-21.
     */
    @Test fun `h7 25мм = 0 минус21`() {
        val r = IsoCalculator.compute(25.0, "h", "IT7"); assertNotNull(r)
        r!!
        assertEquals(0.0, r.esMicrons, 1e-6)
        assertEquals(-21.0, r.eiMicrons, 1e-6)
    }

    /**
     * g6 для 25 мм: es=-7, ei=-7-13=-20.
     */
    @Test fun `g6 25мм = минус7 минус20`() {
        val r = IsoCalculator.compute(25.0, "g", "IT6"); assertNotNull(r)
        r!!
        assertEquals(-7.0, r.esMicrons, 1e-6)
        assertEquals(-20.0, r.eiMicrons, 1e-6)
    }

    /**
     * H8 25мм: EI=0, ES=+33.
     */
    @Test fun `H8 25мм = 0 плюс33`() {
        val r = IsoCalculator.compute(25.0, "H", "IT8"); assertNotNull(r)
        r!!
        assertEquals(33.0, r.esMicrons, 1e-6)
        assertEquals(0.0, r.eiMicrons, 1e-6)
    }

    /**
     * js6 для 25 мм: симметрично ±6.5.
     */
    @Test fun `js6 25мм симметрично`() {
        val r = IsoCalculator.compute(25.0, "js", "IT6"); assertNotNull(r)
        r!!
        assertEquals(6.5, r.esMicrons, 1e-6)
        assertEquals(-6.5, r.eiMicrons, 1e-6)
    }
}
