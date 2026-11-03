package be.corsa.launcher.ui

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** La Corsa vue de profil, dessinée en vectoriel aux couleurs du thème. */
fun carVector(p: Palette): ImageVector {
    val b = ImageVector.Builder(
        defaultWidth = 400.dp,
        defaultHeight = 160.dp,
        viewportWidth = 400f,
        viewportHeight = 160f,
    )
    fun fill(d: String, color: androidx.compose.ui.graphics.Color) {
        b.addPath(pathData = addPathNodes(d), fill = SolidColor(color))
    }
    fun line(d: String, color: androidx.compose.ui.graphics.Color, width: Float) {
        b.addPath(pathData = addPathNodes(d), stroke = SolidColor(color), strokeLineWidth = width)
    }

    fill("M24 150 a176 7 0 1 0 352 0 a176 7 0 1 0 -352 0 Z", p.shadow)
    fill(
        "M18 112 C16 100 22 94 40 90 L112 80 C130 66 150 52 172 46 C200 40 260 38 292 42 " +
            "C318 46 340 62 356 74 C372 80 384 84 386 94 L388 112 C388 118 384 120 378 120 L336 120 " +
            "A32 32 0 0 0 272 120 L128 120 A32 32 0 0 0 64 120 L28 120 C22 120 18 118 18 112 Z",
        p.carBody,
    )
    fill(
        "M30 112 L386 112 L386 116 C386 119 383 120 378 120 L336 120 A32 32 0 0 0 272 120 L128 120 " +
            "A32 32 0 0 0 64 120 L28 120 C24 120 21 118 20 115 Z",
        p.carDark,
    )
    fill("M128 82 C146 66 160 56 176 52 C204 47 256 46 286 49 C306 52 324 62 338 74 L334 78 L130 84 Z", p.glass)
    fill("M228 46 L236 46 L236 84 L228 84 Z", p.carBody)
    line("M128 84 L128 118 M232 84 L234 118 M300 76 L296 100 L272 106", p.seam, 1.6f)
    line("M44 98 L384 101", p.seam, 1.2f)
    fill("M198 90 L214 90 L214 93.5 L198 93.5 Z M286 90 L302 90 L302 93.5 L286 93.5 Z", p.carDark)
    fill("M140 74 L127 72 L125 80 L138 81 Z", p.carDark)
    fill("M22 97 L50 90 L48 97 Z", p.headlight)
    fill("M373 84 L386 91 L386 100 L371 96 Z", androidx.compose.ui.graphics.Color(0xFFE5484D))
    for (cx in listOf(96f, 304f)) {
        fill("M${cx - 25} 124 a25 25 0 1 0 50 0 a25 25 0 1 0 -50 0 Z", p.tire)
        fill("M${cx - 15} 124 a15 15 0 1 0 30 0 a15 15 0 1 0 -30 0 Z", p.rim)
        fill("M${cx - 5} 124 a5 5 0 1 0 10 0 a5 5 0 1 0 -10 0 Z", p.tire)
    }
    return b.build()
}
