package be.corsa.launcher.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import be.corsa.launcher.AppEntry
import be.corsa.launcher.Apps
import be.corsa.launcher.AudioHub
import be.corsa.launcher.LocationHub
import be.corsa.launcher.MediaHub
import be.corsa.launcher.Prefs
import be.corsa.launcher.Sun
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

enum class Sheet { Apps, Search, Music, Settings, Vehicle }

private fun hasLocation(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
fun LauncherApp(demoSheet: String? = null, demoTheme: String? = null) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val mapCtrl = remember { MapController() }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var sheet by remember { mutableStateOf(Sheet.entries.firstOrNull { it.name == demoSheet }) }
    LaunchedEffect(demoTheme) {
        if (demoTheme != null && demoTheme in listOf("jour", "nuit", "auto")) prefs.theme = demoTheme
    }
    var volumeUntil by remember { mutableStateOf(0L) }
    var locationGranted by remember { mutableStateOf(hasLocation(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) LocationHub.start(context)
    }

    LaunchedEffect(Unit) {
        if (locationGranted) {
            LocationHub.start(context)
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
        MediaHub.start(context)
        AudioHub.refresh(context)
        var tick = 0
        while (true) {
            now = System.currentTimeMillis()
            if (tick % 3 == 0 && !MediaHub.granted) MediaHub.start(context)
            tick++
            delay(1000)
        }
    }

    BackHandler(enabled = true) { sheet = null }

    val lat = LocationHub.lat ?: 50.69
    val lon = LocationHub.lon ?: 4.20
    val sun = remember(now / 600_000L, lat, lon) { Sun.times(now, lat, lon) }
    val isDay = when (prefs.theme) {
        "jour" -> true
        "nuit" -> false
        else -> now >= sun.first && now < sun.second
    }
    LauncherScreen(
        p = if (isDay) DayPalette else NightPalette,
        prefs = prefs,
        mapCtrl = mapCtrl,
        now = now,
        sunrise = sun.first,
        sunset = sun.second,
        sheet = sheet,
        onSheet = { sheet = it },
        locationGranted = locationGranted,
        volumeVisible = volumeUntil > now,
        onVolumeChanged = { volumeUntil = System.currentTimeMillis() + 2000 },
    )
}

/** L'écran complet, sans logique de démarrage : utilisé par l'appli et par les captures d'écran. */
@Composable
fun LauncherScreen(
    p: Palette,
    prefs: Prefs,
    mapCtrl: MapController,
    now: Long,
    sunrise: Long,
    sunset: Long,
    sheet: Sheet?,
    onSheet: (Sheet?) -> Unit,
    locationGranted: Boolean,
    volumeVisible: Boolean,
    onVolumeChanged: () -> Unit,
    previewApps: List<AppEntry>? = null,
) {
    val context = LocalContext.current
    val isDay = p.isDay
    val scheme = if (isDay) lightColorScheme(primary = p.accent) else darkColorScheme(primary = p.accent)
    val toggle: (Sheet) -> Unit = { s -> onSheet(if (sheet == s) null else s) }

    MaterialTheme(colorScheme = scheme) {
        Box(Modifier.fillMaxSize().background(p.bg)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    Box(Modifier.weight(0.64f).fillMaxHeight().clipToBounds()) {
                        MapPane(
                            modifier = Modifier.fillMaxSize(),
                            p = p,
                            styleUrl = if (isDay) MAP_STYLE_DAY else MAP_STYLE_NIGHT,
                            locationGranted = locationGranted,
                            ctrl = mapCtrl,
                        )
                        MapOverlays(
                            p = p,
                            prefs = prefs,
                            ctrl = mapCtrl,
                            now = now,
                            locationGranted = locationGranted,
                            onSearch = { onSheet(Sheet.Search) },
                            onMusic = { onSheet(Sheet.Music) },
                            onSettings = { onSheet(Sheet.Settings) },
                        )
                        if (sheet != null) {
                            SheetHost(
                                sheet = sheet,
                                p = p,
                                prefs = prefs,
                                now = now,
                                sunrise = sunrise,
                                sunset = sunset,
                                onClose = { onSheet(null) },
                                onOpen = { onSheet(it) },
                                previewApps = previewApps,
                            )
                        }
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(p.hair))
                    CarPanel(
                        modifier = Modifier.weight(0.36f).fillMaxHeight(),
                        p = p,
                        now = now,
                        onVehicle = { toggle(Sheet.Vehicle) },
                    )
                }
                BottomBar(
                    p = p,
                    sheet = sheet,
                    isDay = isDay,
                    onCar = { toggle(Sheet.Vehicle) },
                    onMap = { onSheet(null) },
                    onMusic = { toggle(Sheet.Music) },
                    onPhone = { Apps.dial(context) },
                    onCamera = { Apps.toast(context, "Caméra à la demande : arrive dès qu'on connaît l'appli caméra de l'autoradio") },
                    onApps = { toggle(Sheet.Apps) },
                    onTheme = { prefs.theme = if (isDay) "nuit" else "jour" },
                    onVoice = { Apps.voice(context) },
                    onVolumeChanged = onVolumeChanged,
                )
            }
            if (volumeVisible) {
                VolumePopup(p, Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 86.dp))
            }
        }
    }
}

@Composable
private fun BoxScope.MapOverlays(
    p: Palette,
    prefs: Prefs,
    ctrl: MapController,
    now: Long,
    locationGranted: Boolean,
    onSearch: () -> Unit,
    onMusic: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.align(Alignment.TopStart).padding(16.dp).width(330.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).card(p, 16.dp).clickable(onClick = onSearch).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = p.text, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text("Où allez-vous ?", color = p.sub, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FavoriteChip("Domicile", Icons.Rounded.Home, p, Modifier.weight(1f)) {
                if (prefs.home.isBlank()) {
                    Apps.toast(context, "Ajoute l'adresse de ton domicile dans Réglages")
                    onSettings()
                } else {
                    Apps.navigate(context, prefs.navApp, prefs.home)
                }
            }
            FavoriteChip("Travail", Icons.Rounded.Work, p, Modifier.weight(1f)) {
                if (prefs.work.isBlank()) {
                    Apps.toast(context, "Ajoute l'adresse de ton travail dans Réglages")
                    onSettings()
                } else {
                    Apps.navigate(context, prefs.navApp, prefs.work)
                }
            }
        }
    }

    Column(
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End,
    ) {
        MapButton(
            icon = if (ctrl.tracking) Icons.Rounded.MyLocation else Icons.Rounded.LocationSearching,
            description = "Recentrer sur la voiture",
            p = p,
            tint = if (ctrl.tracking) p.accent else p.text,
        ) {
            if (!locationGranted) Apps.toast(context, "Autorise la localisation pour suivre la voiture") else ctrl.recenter()
        }
        MapButton(Icons.Rounded.Add, "Zoomer", p) { ctrl.zoomIn() }
        MapButton(Icons.Rounded.Remove, "Dézoomer", p) { ctrl.zoomOut() }
    }

    if (!ctrl.tracking && locationGranted) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 82.dp)
                .height(54.dp)
                .card(p, 27.dp)
                .clickable { ctrl.recenter() }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.NearMe, contentDescription = null, tint = p.accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Recentrer", color = p.accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    MediaCard(p, now, Modifier.align(Alignment.BottomStart).padding(16.dp), onOpen = onMusic)
}

@Composable
private fun FavoriteChip(label: String, icon: ImageVector, p: Palette, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier.height(52.dp).card(p, 14.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = p.accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MediaCard(p: Palette, now: Long, modifier: Modifier, onOpen: () -> Unit) {
    val context = LocalContext.current
    val granted = MediaHub.granted
    val title = MediaHub.title ?: if (granted) "Aucune lecture en cours" else "Musique"
    val subtitle = when {
        !granted -> "Autorise l'accès pour afficher le morceau"
        MediaHub.title == null -> "Lance Spotify, Apple Music ou le Bluetooth"
        else -> listOfNotNull(MediaHub.artist, MediaHub.appName).joinToString(" · ")
    }
    val duration = MediaHub.durationMs
    val fraction = if (duration > 0 && now > 0) (MediaHub.positionMs().toFloat() / duration).coerceIn(0f, 1f) else 0f

    Box(modifier.width(430.dp).height(88.dp).card(p)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxHeight().clickable(onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(p, 64.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = p.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = p.sub, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (granted) {
                IconBtn(Icons.Rounded.SkipPrevious, "Précédent", p, size = 48.dp, iconSize = 30.dp) { MediaHub.prev() }
                IconBtn(
                    if (MediaHub.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (MediaHub.playing) "Pause" else "Lecture",
                    p, size = 52.dp, iconSize = 32.dp, background = p.pill,
                ) { MediaHub.playPause() }
                IconBtn(Icons.Rounded.SkipNext, "Suivant", p, size = 48.dp, iconSize = 30.dp) { MediaHub.next() }
            } else {
                PillButton("Autoriser", p, selected = true) { Apps.openNotificationAccess(context) }
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(fraction)
                .height(3.dp)
                .background(p.text)
        )
    }
}

@Composable
private fun CarPanel(modifier: Modifier, p: Palette, now: Long, onVehicle: () -> Unit) {
    val car = remember(p) { carVector(p) }
    val timeText = remember(now / 1000) { SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(now)) }
    val dateText = remember(now / 60_000) { SimpleDateFormat("EEE d MMM", Locale.FRANCE).format(Date(now)) }
    val speed = LocationHub.speedKmh

    Column(modifier.background(p.bg).padding(horizontal = 26.dp, vertical = 18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = p.text, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(6.dp))
            Text("Corsa D", color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(dateText, color = p.sub, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(12.dp))
            Text(timeText, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = speed?.toString() ?: "—",
                color = p.text,
                fontSize = 88.sp,
                lineHeight = 88.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(8.dp))
            Text("km/h", color = p.sub, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 14.dp))
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 14.dp)) {
                Text(if (speed == null) "GPS" else "GPS actif", color = p.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(if (speed == null) "en attente du signal" else "vitesse mesurée", color = p.sub, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(14.dp))
        Image(
            painter = rememberVectorPainter(car),
            contentDescription = "Voir les informations du véhicule",
            modifier = Modifier.fillMaxWidth().aspectRatio(2.5f).clickable(onClick = onVehicle),
        )
        Spacer(Modifier.weight(1f))
        StatusRow(Icons.Rounded.Lock, "Portes et hayon", "boîtier CANbus", p, onVehicle)
        StatusRow(Icons.Rounded.Speed, "Conso et moteur", "boîtier OBD2", p, onVehicle)
        StatusRow(Icons.Rounded.BatteryFull, "Pneus et batterie", "à brancher", p, onVehicle)
    }
}

@Composable
private fun StatusRow(icon: ImageVector, label: String, value: String, p: Palette, onClick: () -> Unit) {
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.hair))
        Row(
            modifier = Modifier.fillMaxWidth().height(54.dp).clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = p.sub, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, color = p.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(value, color = p.sub, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = p.sub, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun BottomBar(
    p: Palette,
    sheet: Sheet?,
    isDay: Boolean,
    onCar: () -> Unit,
    onMap: () -> Unit,
    onMusic: () -> Unit,
    onPhone: () -> Unit,
    onCamera: () -> Unit,
    onApps: () -> Unit,
    onTheme: () -> Unit,
    onVoice: () -> Unit,
    onVolumeChanged: () -> Unit,
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().background(p.bar)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.hair))
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarItem(Icons.Rounded.DirectionsCar, "Véhicule", p, sheet == Sheet.Vehicle, onCar)
            Spacer(Modifier.weight(1f))
            BarItem(Icons.Rounded.NearMe, "Carte", p, sheet == null || sheet == Sheet.Search, onMap)
            Spacer(Modifier.width(6.dp))
            BarItem(Icons.Rounded.MusicNote, "Musique", p, sheet == Sheet.Music, onMusic)
            Spacer(Modifier.width(6.dp))
            BarItem(Icons.Rounded.Call, "Téléphone", p, false, onPhone)
            Spacer(Modifier.width(6.dp))
            BarItem(Icons.Rounded.Videocam, "Caméra", p, false, onCamera)
            Spacer(Modifier.width(6.dp))
            BarItem(Icons.Rounded.Apps, "Applis", p, sheet == Sheet.Apps || sheet == Sheet.Settings, onApps)
            Spacer(Modifier.weight(1f))
            IconBtn(if (isDay) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, if (isDay) "Passer en mode nuit" else "Passer en mode jour", p, size = 56.dp, onClick = onTheme)
            IconBtn(Icons.Rounded.Mic, "Commande vocale", p, size = 56.dp, onClick = onVoice)
            IconBtn(Icons.Rounded.ChevronLeft, "Baisser le volume", p, size = 48.dp, iconSize = 30.dp, tint = p.sub) {
                AudioHub.down(context); onVolumeChanged()
            }
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { AudioHub.toggleMute(context); onVolumeChanged() }
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(volumeIcon(), contentDescription = if (AudioHub.muted) "Rétablir le son" else "Couper le son", tint = p.text, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (AudioHub.muted) "0" else AudioHub.level.toString(), color = p.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            IconBtn(Icons.Rounded.ChevronRight, "Monter le volume", p, size = 48.dp, iconSize = 30.dp, tint = p.sub) {
                AudioHub.up(context); onVolumeChanged()
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun volumeIcon(): ImageVector = when {
    AudioHub.muted || AudioHub.level == 0 -> Icons.Rounded.VolumeOff
    AudioHub.level < AudioHub.max / 2 -> Icons.Rounded.VolumeDown
    else -> Icons.Rounded.VolumeUp
}

@Composable
private fun BarItem(icon: ImageVector, label: String, p: Palette, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 72.dp, height = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) p.pill else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (active) p.accent else p.text, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun VolumePopup(p: Palette, modifier: Modifier) {
    val level = if (AudioHub.muted) 0 else AudioHub.level
    val fraction = (level.toFloat() / AudioHub.max).coerceIn(0f, 1f)
    Row(
        modifier = modifier.width(280.dp).card(p).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(volumeIcon(), contentDescription = null, tint = p.text, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(p.pill)) {
            Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(p.text))
        }
        Spacer(Modifier.width(12.dp))
        Text(level.toString(), color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Suppress("unused")
private fun Modifier.outlined(p: Palette): Modifier = this.border(1.dp, p.hair, RoundedCornerShape(18.dp))
