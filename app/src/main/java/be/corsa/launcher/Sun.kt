package be.corsa.launcher

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

/** Heures de lever et de coucher du soleil, pour le mode jour/nuit automatique. */
object Sun {

    /** Retourne (lever, coucher) en millisecondes depuis 1970, pour le jour de [now]. */
    fun times(now: Long, lat: Double, lng: Double): Pair<Long, Long> {
        val rad = PI / 180.0
        val dayMs = 86_400_000.0
        val j1970 = 2_440_588.0
        val j2000 = 2_451_545.0
        val j0 = 0.0009
        val e = rad * 23.4397

        val d = now / dayMs - 0.5 + j1970 - j2000
        val lw = rad * -lng
        val phi = rad * lat
        val n = (d - j0 - lw / (2 * PI)).roundToLong().toDouble()
        val ds = j0 + lw / (2 * PI) + n
        val m = rad * (357.5291 + 0.98560028 * ds)
        val c = rad * (1.9148 * sin(m) + 0.02 * sin(2 * m) + 0.0003 * sin(3 * m))
        val l = m + c + rad * 102.9372 + PI
        val dec = asin(sin(e) * sin(l))

        fun transit(a: Double) = j2000 + a + 0.0053 * sin(m) - 0.0069 * sin(2 * l)

        val jNoon = transit(ds)
        val cosW = (sin(-0.833 * rad) - sin(phi) * sin(dec)) / (cos(phi) * cos(dec))
        val w = acos(cosW.coerceIn(-1.0, 1.0))
        val jSet = transit(j0 + (w + lw) / (2 * PI) + n)
        val jRise = jNoon - (jSet - jNoon)

        fun toMs(j: Double) = ((j + 0.5 - j1970) * dayMs).toLong()
        return toMs(jRise) to toMs(jSet)
    }
}
