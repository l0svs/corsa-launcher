package be.corsa.launcher.ui

import androidx.compose.ui.graphics.Color

/** Couleurs de l'interface, en mode jour (lisible au soleil) et en mode nuit. */
data class Palette(
    val isDay: Boolean,
    val bg: Color,
    val card: Color,
    val hair: Color,
    val text: Color,
    val sub: Color,
    val pill: Color,
    val inv: Color,
    val accent: Color,
    val onAccent: Color,
    val ok: Color,
    val warn: Color,
    val danger: Color,
    val bar: Color,
    val carBody: Color,
    val carDark: Color,
    val glass: Color,
    val tire: Color,
    val rim: Color,
    val seam: Color,
    val shadow: Color,
    val headlight: Color,
)

val DayPalette = Palette(
    isDay = true,
    bg = Color(0xFFF4F5F7),
    card = Color(0xFFFFFFFF),
    hair = Color(0xFFE1E4E8),
    text = Color(0xFF15171A),
    sub = Color(0xFF5E6670),
    pill = Color(0xFFECEEF1),
    inv = Color(0xFFFFFFFF),
    accent = Color(0xFF3B6EF5),
    onAccent = Color(0xFFFFFFFF),
    ok = Color(0xFF1F8A4C),
    warn = Color(0xFFB26A00),
    danger = Color(0xFFE5484D),
    bar = Color(0xFFFFFFFF),
    carBody = Color(0xFFD5D9DF),
    carDark = Color(0xFFB9BFC7),
    glass = Color(0xFF2F353D),
    tire = Color(0xFF1A1C1F),
    rim = Color(0xFFA7AEB7),
    seam = Color(0x1F000000),
    shadow = Color(0x14000000),
    headlight = Color(0xFFF6F7F9),
)

val NightPalette = Palette(
    isDay = false,
    bg = Color(0xFF121418),
    card = Color(0xFF1C1F24),
    hair = Color(0xFF2A2E35),
    text = Color(0xFFF2F4F7),
    sub = Color(0xFF9AA3AE),
    pill = Color(0xFF2A2E35),
    inv = Color(0xFF0E1012),
    accent = Color(0xFF6E95FF),
    onAccent = Color(0xFF0E1012),
    ok = Color(0xFF3DD68C),
    warn = Color(0xFFF5A524),
    danger = Color(0xFFFF5A5F),
    bar = Color(0xFF0D0F12),
    carBody = Color(0xFF4A4F58),
    carDark = Color(0xFF383C44),
    glass = Color(0xFF0F1114),
    tire = Color(0xFF050607),
    rim = Color(0xFF8D949D),
    seam = Color(0x1AFFFFFF),
    shadow = Color(0x80000000),
    headlight = Color(0xFFE9EEF5),
)

/** Styles de carte libres et gratuits (OpenFreeMap, données OpenStreetMap). */
const val MAP_STYLE_DAY = "https://tiles.openfreemap.org/styles/positron"
const val MAP_STYLE_NIGHT = "https://tiles.openfreemap.org/styles/dark"
