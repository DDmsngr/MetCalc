package tech.deepdrift.metallist.domain.calc

import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.domain.model.ShapeParams
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Площадь поперечного сечения (мм²) для 11 форм проката.
 *
 * Все входные размеры — в миллиметрах.
 * Все формулы — упрощённые, без учёта скруглений и уклонов полок
 * (это стандартная точность для инженерных калькуляторов проката).
 */
object CrossSection {

    fun area(shape: ProfileShape, p: ShapeParams): Double = when (shape) {
        ProfileShape.Round -> round(p.d)
        ProfileShape.PipeRound -> pipeRound(p.d, p.d2)
        ProfileShape.Hex -> hex(p.d)
        ProfileShape.Plate -> sheet(p.t, p.b)
        ProfileShape.BentChannel -> bentChannel(p.h, p.b, p.t)
        ProfileShape.PipeRect -> pipeRect(p.h, p.b, p.t)
        ProfileShape.Angle -> angleEqual(p.b, p.t)
        ProfileShape.IBeam -> iBeam(p.h, p.b, p.s, p.t)
        ProfileShape.Channel -> channel(p.h, p.b, p.s, p.t)
        ProfileShape.Rebar -> round(p.d)
    }

    /** Круг сплошной, диаметр D. A = πD²/4 */
    fun round(d: Double): Double = PI * d * d / 4.0

    /** Труба круглая, D наружн., d внутр. */
    fun pipeRound(dOuter: Double, dInner: Double): Double =
        PI * (dOuter * dOuter - dInner * dInner) / 4.0

    /** Квадрат сплошной, сторона a. */
    fun square(a: Double): Double = a * a

    /**
     * Шестигранник правильный.
     * Размер "под ключ" S = диаметр вписанной окружности.
     * A = (√3/2)·S² = 0.8660254·S²
     */
    fun hex(s: Double): Double = (sqrt(3.0) / 2.0) * s * s

    /** Лист / полоса: толщина × ширина. */
    fun sheet(t: Double, w: Double): Double = t * w

    /**
     * Швеллер гнутый (П-образный из полосы толщины t).
     * H — высота стенки (внешняя), B — ширина полки (внешняя), t — толщина стенки.
     * Развёртка полосы: A = t · (H + 2·(B - t))
     */
    fun bentChannel(h: Double, b: Double, t: Double): Double =
        t * (h + 2.0 * (b - t))

    /** Труба прямоугольная. H, B — внешние; t — толщина стенки. */
    fun pipeRect(h: Double, b: Double, t: Double): Double =
        h * b - (h - 2.0 * t) * (b - 2.0 * t)

    /** Уголок равнополочный. b — полка, t — толщина. A = t·(2·b - t) */
    fun angleEqual(b: Double, t: Double): Double = t * (2.0 * b - t)

    /**
     * Двутавр (I-beam) упрощённо.
     * h — высота, b — ширина полки, s — толщина стенки, t — средняя толщина полки.
     * A = 2·b·t + (h - 2·t)·s
     */
    fun iBeam(h: Double, b: Double, s: Double, t: Double): Double =
        2.0 * b * t + (h - 2.0 * t) * s

    /** Швеллер (C-beam) — та же геометрия что и двутавр (упрощённо). */
    fun channel(h: Double, b: Double, s: Double, t: Double): Double =
        iBeam(h, b, s, t)
}
