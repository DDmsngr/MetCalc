package tech.deepdrift.metallist.domain.iso

/**
 * Основные отклонения ISO 286-1:2010 (Table 2 — валы, Table 3 — отверстия).
 *
 * Реализованы наиболее применяемые поля допусков:
 * - валы (строчные): a, b, c, d, e, f, g, h, js, k, m, n, p, r, s, t, u
 * - отверстия (прописные): A, B, C, D, E, F, G, H, JS, K, M, N, P, R, S, T, U
 *
 * Для полей a..g значение — это верхнее отклонение es (в мкм), отрицательное.
 * Для поля h: es = 0.
 * Для полей k..u значение — это нижнее отклонение ei (в мкм), положительное.
 * Для js: симметричное поле, es = +IT/2, ei = -IT/2.
 *
 * Отверстия рассчитываются по «общему правилу» ISO 286-1: EI(letter) = -es(letter_lower)
 * для полей A..G и EI(H) = 0; ES(letter) = -ei(letter_lower) для K..U — эта формула
 * корректна для квалитетов IT8 и грубее для K/M/N и IT7 и грубее для P..U
 * (в MVP правило перехода shift δ = IT_n - IT_(n-1) не применяется).
 *
 * Все табличные значения соответствуют официальной ISO 286-1:2010.
 * 13 интервалов номинальных размеров (совпадают с [IsoTolerances.ranges]).
 */
object IsoDeviations {

    /** Список букв основных отклонений валов, порядок фиксирован для индексации в таблицах. */
    private val shaftLowerLetters = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    private val shaftUpperLetters = listOf("k", "m", "n", "p", "r", "s", "t", "u")

    /**
     * es (мкм) для валов a..h. Индексация: [rangeIdx][letterIdx].
     * Значения из ISO 286-1:2010 Table 2 (для упрощённой сетки 13 интервалов
     * взято значение первого подынтервала, где ISO расщепляет диапазон).
     */
    private val shaftEsLower: Array<IntArray> = arrayOf(
        // 0-3:     a     b    c    d    e    f    g   h
        intArrayOf(-270, -140, -60, -20, -14, -6,  -2, 0),
        // 3-6
        intArrayOf(-270, -140, -70, -30, -20, -10, -4, 0),
        // 6-10
        intArrayOf(-280, -150, -80, -40, -25, -13, -5, 0),
        // 10-18
        intArrayOf(-290, -150, -95, -50, -32, -16, -6, 0),
        // 18-30
        intArrayOf(-300, -160, -110, -65, -40, -20, -7, 0),
        // 30-50
        intArrayOf(-310, -170, -120, -80, -50, -25, -9, 0),
        // 50-80
        intArrayOf(-340, -190, -140, -100, -60, -30, -10, 0),
        // 80-120
        intArrayOf(-380, -220, -170, -120, -72, -36, -12, 0),
        // 120-180
        intArrayOf(-460, -260, -200, -145, -85, -43, -14, 0),
        // 180-250
        intArrayOf(-660, -340, -240, -170, -100, -50, -15, 0),
        // 250-315
        intArrayOf(-920, -480, -300, -190, -110, -56, -17, 0),
        // 315-400
        intArrayOf(-1200, -600, -360, -210, -125, -62, -18, 0),
        // 400-500
        intArrayOf(-1500, -760, -440, -230, -135, -68, -20, 0),
    )

    /**
     * ei (мкм) для валов k..u. Индексация: [rangeIdx][letterIdx].
     * Значения из ISO 286-1:2010 Table 2.
     */
    private val shaftEiUpper: Array<IntArray> = arrayOf(
        // 0-3:     k  m  n  p  r  s  t   u
        intArrayOf(0, 2, 4, 6, 10, 14, 0, 18),   // t не определено для 0-3, 0
        // 3-6
        intArrayOf(1, 4, 8, 12, 15, 19, 0, 23),
        // 6-10
        intArrayOf(1, 6, 10, 15, 19, 23, 0, 28),
        // 10-18
        intArrayOf(1, 7, 12, 18, 23, 28, 0, 33),
        // 18-30
        intArrayOf(2, 8, 15, 22, 28, 35, 41, 41),
        // 30-50
        intArrayOf(2, 9, 17, 26, 34, 43, 48, 60),
        // 50-80 (r,s,t делятся ISO — берём средние 50-65)
        intArrayOf(2, 11, 20, 32, 41, 53, 66, 87),
        // 80-120 (берём 80-100)
        intArrayOf(3, 13, 23, 37, 51, 71, 91, 124),
        // 120-180 (берём 120-140)
        intArrayOf(3, 15, 27, 43, 63, 92, 122, 170),
        // 180-250 (берём 180-200)
        intArrayOf(4, 17, 31, 50, 77, 121, 166, 236),
        // 250-315 (берём 250-280)
        intArrayOf(4, 20, 34, 56, 94, 158, 218, 313),
        // 315-400 (берём 315-355)
        intArrayOf(4, 21, 37, 62, 108, 190, 268, 390),
        // 400-500 (берём 400-450)
        intArrayOf(5, 23, 40, 68, 126, 232, 330, 490),
    )

    /**
     * Пара (es, ei) в мкм для указанного поля.
     *
     * @param letter буква поля допуска (напр. "h", "g", "H", "K", "js", "JS")
     * @param sizeMm номинальный размер
     * @param grade квалитет (напр. "IT7", "IT10"), используется для JS и вычисления парного отклонения
     * @return пара (верхнее, нижнее) отклонение в мкм или null, если поле не поддерживается
     */
    fun deviations(letter: String, sizeMm: Double, grade: String): Deviation? {
        val it = IsoTolerances.itMicrons(sizeMm, grade) ?: return null
        val ri = IsoTolerances.rangeIndexFor(sizeMm)
        if (ri < 0) return null

        val letterLower = letter.lowercase()
        val isShaft = letter.first().isLowerCase() || letter == "js"

        if (letterLower == "js") {
            val half = it / 2.0
            return Deviation(es = +half, ei = -half)
        }

        // Валы
        if (isShaft) {
            val li = shaftLowerLetters.indexOf(letterLower)
            if (li != -1) {
                val es = shaftEsLower[ri][li].toDouble()
                return Deviation(es = es, ei = es - it)
            }
            val ui = shaftUpperLetters.indexOf(letterLower)
            if (ui != -1) {
                val ei = shaftEiUpper[ri][ui].toDouble()
                return Deviation(es = ei + it, ei = ei)
            }
            return null
        }

        // Отверстия — общее правило ISO 286-1: EI(A..G)=-es(a..g), ES(K..U)=-ei(k..u).
        val li = shaftLowerLetters.indexOf(letterLower)
        if (li != -1) {
            val ei = -shaftEsLower[ri][li].toDouble()
            return Deviation(es = ei + it, ei = ei)
        }
        val ui = shaftUpperLetters.indexOf(letterLower)
        if (ui != -1) {
            val es = -shaftEiUpper[ri][ui].toDouble()
            return Deviation(es = es, ei = es - it)
        }
        return null
    }

    val shaftLetters: List<String> = shaftLowerLetters + listOf("js") + shaftUpperLetters
    val holeLetters: List<String> =
        shaftLowerLetters.map { it.uppercase() } + listOf("JS") + shaftUpperLetters.map { it.uppercase() }

    data class Deviation(val es: Double, val ei: Double)
}
