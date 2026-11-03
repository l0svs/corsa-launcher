package be.corsa.launcher

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/** Réglages enregistrés sur l'autoradio, lus par l'interface en direct. */
class Prefs(context: Context) {

    private val sp = context.applicationContext.getSharedPreferences("corsa", Context.MODE_PRIVATE)

    private val themeState = mutableStateOf(sp.getString(KEY_THEME, "auto") ?: "auto")
    private val navAppState = mutableStateOf(sp.getString(KEY_NAV, "waze") ?: "waze")
    private val homeState = mutableStateOf(sp.getString(KEY_HOME, "") ?: "")
    private val workState = mutableStateOf(sp.getString(KEY_WORK, "") ?: "")

    /** "nuit", "jour" ou "auto" (selon le soleil). */
    var theme: String
        get() = themeState.value
        set(value) { themeState.value = value; save(KEY_THEME, value) }

    /** "waze" ou "maps". */
    var navApp: String
        get() = navAppState.value
        set(value) { navAppState.value = value; save(KEY_NAV, value) }

    var home: String
        get() = homeState.value
        set(value) { homeState.value = value; save(KEY_HOME, value) }

    var work: String
        get() = workState.value
        set(value) { workState.value = value; save(KEY_WORK, value) }

    private fun save(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_NAV = "nav_app"
        const val KEY_HOME = "fav_home"
        const val KEY_WORK = "fav_work"
    }
}
