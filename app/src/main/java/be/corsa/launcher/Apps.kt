package be.corsa.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(val label: String, val pkg: String, val icon: ImageBitmap?)

/** Actions vers les autres applis de l'autoradio. */
object Apps {

    const val WAZE = "com.waze"
    const val GOOGLE_MAPS = "com.google.android.apps.maps"

    /** Mots qui permettent de reconnaître l'appli CarPlay d'origine, quel que soit le fabricant. */
    private val carPlayHints = listOf("carplay", "zlink", "autokit", "carlink", "carbit", "tlink", "easyconnect")

    fun load(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val activities = pm.queryIntentActivities(intent, 0)
        return activities.asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                AppEntry(
                    label = ri.loadLabel(pm).toString(),
                    pkg = ri.activityInfo.packageName,
                    icon = runCatching { ri.loadIcon(pm).toBitmap(128, 128).asImageBitmap() }.getOrNull()
                )
            }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun isCarPlay(app: AppEntry): Boolean {
        val s = (app.label + " " + app.pkg).lowercase()
        return carPlayHints.any { s.contains(it) }
    }

    /** Vraie icône d'une appli installée, ou null si elle n'est pas sur l'autoradio. */
    fun icon(context: Context, pkg: String, sizePx: Int = 128): ImageBitmap? = try {
        context.packageManager.getApplicationIcon(pkg).toBitmap(sizePx, sizePx).asImageBitmap()
    } catch (e: Exception) {
        null
    }

    fun isInstalled(context: Context, pkg: String): Boolean =
        context.packageManager.getLaunchIntentForPackage(pkg) != null

    fun launch(context: Context, pkg: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(context, intent)
    }

    fun navigate(context: Context, app: String, query: String) {
        val q = Uri.encode(query)
        val primary = if (app == "maps") {
            Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$q")).setPackage(GOOGLE_MAPS)
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://waze.com/ul?q=$q&navigate=yes")).setPackage(WAZE)
        }
        if (start(context, primary.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))) return
        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$q")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (!start(context, fallback)) toast(context, "Aucune appli de navigation installée")
    }

    fun dial(context: Context) {
        if (!start(context, Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))) {
            toast(context, "Aucune appli téléphone trouvée sur l'autoradio")
        }
    }

    fun voice(context: Context) {
        val candidates = listOf(Intent(Intent.ACTION_VOICE_COMMAND), Intent(Intent.ACTION_ASSIST))
        for (intent in candidates) {
            if (start(context, intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))) return
        }
        toast(context, "Aucun assistant vocal installé")
    }

    fun openHomeSettings(context: Context) = openSettings(context, Settings.ACTION_HOME_SETTINGS)

    fun openNotificationAccess(context: Context) = openSettings(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun openAndroidSettings(context: Context) = openSettings(context, Settings.ACTION_SETTINGS)

    private fun openSettings(context: Context, action: String) {
        if (!start(context, Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))) {
            toast(context, "Ce réglage n'existe pas sur cet autoradio")
        }
    }

    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}
