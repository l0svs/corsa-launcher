package be.corsa.launcher

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat

/** Service déclaré dans le manifeste : donne accès aux sessions musicales des autres applis. */
class MediaListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        MediaHub.start(applicationContext)
    }
}

/** Musique en cours, quelle que soit l'appli (Spotify, Apple Music, Bluetooth de l'autoradio…). */
object MediaHub {
    var granted by mutableStateOf(false)
        private set
    var title by mutableStateOf<String?>(null)
        private set
    var artist by mutableStateOf<String?>(null)
        private set
    var art by mutableStateOf<Bitmap?>(null)
        private set
    var appName by mutableStateOf<String?>(null)
        private set
    var pkg by mutableStateOf<String?>(null)
        private set
    var playing by mutableStateOf(false)
        private set
    var durationMs by mutableStateOf(0L)
        private set

    private var posMs = 0L
    private var posAt = 0L
    private var speed = 1f
    private var controller: MediaController? = null
    private var sessionsListener: MediaSessionManager.OnActiveSessionsChangedListener? = null
    private val main = Handler(Looper.getMainLooper())
    private var appContext: Context? = null

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            readMeta(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            readState(state)
        }

        override fun onSessionDestroyed() {
            bind(null)
        }
    }

    fun isListenerEnabled(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    fun start(context: Context) {
        val app = context.applicationContext
        appContext = app
        if (!isListenerEnabled(app)) {
            granted = false
            return
        }
        try {
            val msm = app.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val component = ComponentName(app, MediaListener::class.java)
            if (sessionsListener == null) {
                val listener = MediaSessionManager.OnActiveSessionsChangedListener { list -> pick(list) }
                msm.addOnActiveSessionsChangedListener(listener, component, main)
                sessionsListener = listener
            }
            pick(msm.getActiveSessions(component))
            granted = true
        } catch (e: SecurityException) {
            granted = false
        }
    }

    private fun pick(list: List<MediaController>?) {
        val c = list?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: list?.firstOrNull()
        val current = controller
        if (c == null || current == null || c.sessionToken != current.sessionToken) bind(c)
    }

    private fun bind(c: MediaController?) {
        controller?.unregisterCallback(callback)
        controller = c
        if (c == null) {
            title = null; artist = null; art = null; appName = null; pkg = null
            playing = false; durationMs = 0L
            return
        }
        c.registerCallback(callback, main)
        pkg = c.packageName
        appName = try {
            val pm = appContext!!.packageManager
            @Suppress("DEPRECATION")
            val info = pm.getApplicationInfo(c.packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            c.packageName
        }
        readMeta(c.metadata)
        readState(c.playbackState)
    }

    private fun readMeta(m: MediaMetadata?) {
        title = m?.getString(MediaMetadata.METADATA_KEY_TITLE)
        artist = m?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: m?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        art = m?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: m?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        durationMs = m?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
    }

    private fun readState(s: PlaybackState?) {
        playing = s?.state == PlaybackState.STATE_PLAYING
        posMs = s?.position ?: 0L
        posAt = s?.lastPositionUpdateTime ?: SystemClock.elapsedRealtime()
        speed = s?.playbackSpeed ?: 1f
    }

    /** Position actuelle dans le morceau, recalculée à chaque appel. */
    fun positionMs(): Long {
        val p = if (playing) posMs + ((SystemClock.elapsedRealtime() - posAt) * speed).toLong() else posMs
        return if (durationMs > 0) p.coerceIn(0L, durationMs) else p.coerceAtLeast(0L)
    }

    fun playPause() {
        val c = controller ?: return
        if (playing) c.transportControls.pause() else c.transportControls.play()
    }

    fun next() { controller?.transportControls?.skipToNext() }

    fun prev() { controller?.transportControls?.skipToPrevious() }

    fun seekTo(ms: Long) { controller?.transportControls?.seekTo(ms) }
}

/** Position et vitesse GPS de l'autoradio. */
object LocationHub {
    var speedKmh by mutableStateOf<Int?>(null)
        private set
    var lat by mutableStateOf<Double?>(null)
        private set
    var lon by mutableStateOf<Double?>(null)
        private set

    private var started = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lat = location.latitude
            lon = location.longitude
            if (location.hasSpeed()) speedKmh = (location.speed * 3.6f).toInt()
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun start(context: Context) {
        if (started) return
        val lm = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val providers = lm.allProviders
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { lat = it.latitude; lon = it.longitude }
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, listener, Looper.getMainLooper())
            }
            if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                if (lat == null) lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { lat = it.latitude; lon = it.longitude }
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 0f, listener, Looper.getMainLooper())
            }
            started = true
        } catch (e: Exception) {
            started = false
        }
    }
}

/** Volume du son multimédia de l'autoradio. */
object AudioHub {
    var level by mutableStateOf(0)
        private set
    var max by mutableStateOf(15)
        private set
    var muted by mutableStateOf(false)
        private set

    private fun am(context: Context) = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun refresh(context: Context) {
        val a = am(context)
        level = a.getStreamVolume(AudioManager.STREAM_MUSIC)
        max = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        muted = a.isStreamMute(AudioManager.STREAM_MUSIC)
    }

    fun up(context: Context) = adjust(context, AudioManager.ADJUST_RAISE)

    fun down(context: Context) = adjust(context, AudioManager.ADJUST_LOWER)

    fun toggleMute(context: Context) = adjust(context, AudioManager.ADJUST_TOGGLE_MUTE)

    private fun adjust(context: Context, direction: Int) {
        try {
            am(context).adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
        } catch (e: SecurityException) {
            // Certains autoradios bloquent le réglage du volume par les applis.
        }
        refresh(context)
    }
}
