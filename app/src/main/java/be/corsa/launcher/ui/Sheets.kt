package be.corsa.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.corsa.launcher.AppEntry
import be.corsa.launcher.Apps
import be.corsa.launcher.LocationHub
import be.corsa.launcher.MediaHub
import be.corsa.launcher.Prefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Panneau posé sur la carte (applis, musique, recherche, réglages, véhicule). */
@Composable
fun BoxScope.SheetHost(
    sheet: Sheet,
    p: Palette,
    prefs: Prefs,
    now: Long,
    sunrise: Long,
    sunset: Long,
    onClose: () -> Unit,
    onOpen: (Sheet) -> Unit,
) {
    val tall = sheet == Sheet.Settings || sheet == Sheet.Vehicle || sheet == Sheet.Search
    val place = if (tall) {
        Modifier.align(Alignment.TopStart).padding(16.dp).fillMaxSize()
    } else {
        Modifier.align(Alignment.BottomStart).padding(16.dp).fillMaxWidth()
    }
    Column(
        modifier = place
            .card(p, 22.dp)
            .blockTouches()
            .padding(22.dp),
    ) {
        when (sheet) {
            Sheet.Apps -> AppsSheet(p, onClose, onSettings = { onOpen(Sheet.Settings) })
            Sheet.Search -> SearchSheet(p, prefs, onClose)
            Sheet.Music -> MusicSheet(p, now, onClose)
            Sheet.Settings -> SettingsSheet(p, prefs, sunrise, sunset, onClose)
            Sheet.Vehicle -> VehicleSheet(p, onClose)
        }
    }
}

@Composable
private fun ColumnScope.AppsSheet(p: Palette, onClose: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppEntry>?>(null) }
    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { Apps.load(context) }
    }
    SheetHeader("Applis", p, onClose = onClose)
    Spacer(Modifier.height(16.dp))
    val list = apps
    if (list == null) {
        Text("Chargement des applis…", color = p.sub, fontSize = 16.sp)
        return
    }
    val carPlay = list.firstOrNull { Apps.isCarPlay(it) }
    val others = list.filter { it.pkg != carPlay?.pkg }
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SpecialTile("CarPlay", Icons.Rounded.PhoneIphone, Color(0xFF2B2F36), p) {
                if (carPlay == null || !Apps.launch(context, carPlay.pkg)) {
                    Apps.toast(context, "Appli CarPlay introuvable : envoie-moi son nom (Paramètres › Applications)")
                }
                onClose()
            }
        }
        item {
            SpecialTile("Réglages", Icons.Rounded.Settings, Color(0xFF7A818B), p, onClick = onSettings)
        }
        items(others, key = { it.pkg }) { app ->
            AppTile(app, p) {
                if (!Apps.launch(context, app.pkg)) Apps.toast(context, "Impossible d'ouvrir ${app.label}")
                onClose()
            }
        }
    }
}

@Composable
private fun AppTile(app: AppEntry, p: Palette, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val icon = app.icon
        if (icon != null) {
            Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(68.dp).clip(RoundedCornerShape(18.dp)))
        } else {
            Box(Modifier.size(68.dp).clip(RoundedCornerShape(18.dp)).background(p.pill))
        }
        Spacer(Modifier.height(8.dp))
        Text(app.label, color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SpecialTile(label: String, icon: ImageVector, color: Color, p: Palette, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(68.dp).clip(RoundedCornerShape(18.dp)).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SearchSheet(p: Palette, prefs: Prefs, onClose: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val go: (String) -> Unit = { destination ->
        Apps.navigate(context, prefs.navApp, destination)
        onClose()
    }
    val appName = if (prefs.navApp == "maps") "Google Maps" else "Waze"
    SheetHeader("Où allez-vous ?", p, subtitle = "Le guidage s'ouvre dans $appName", onClose = onClose)
    Spacer(Modifier.height(14.dp))
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Adresse, lieu ou commerce") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { if (query.isNotBlank()) go(query.trim()) }),
        colors = fieldColors(p),
    )
    Spacer(Modifier.height(10.dp))
    Column(Modifier.verticalScroll(rememberScrollState())) {
        if (query.isNotBlank()) {
            DestinationRow(Icons.Rounded.LocationOn, "Rechercher « ${query.trim()} »", "Adresse", p) { go(query.trim()) }
        }
        DestinationRow(Icons.Rounded.Home, "Domicile", prefs.home.ifBlank { "À renseigner dans Réglages" }, p) {
            if (prefs.home.isBlank()) Apps.toast(context, "Ajoute l'adresse dans Réglages") else go(prefs.home)
        }
        DestinationRow(Icons.Rounded.Work, "Travail", prefs.work.ifBlank { "À renseigner dans Réglages" }, p) {
            if (prefs.work.isBlank()) Apps.toast(context, "Ajoute l'adresse dans Réglages") else go(prefs.work)
        }
        DestinationRow(Icons.Rounded.LocalGasStation, "Station-service", "La plus proche", p) { go("station-service") }
        DestinationRow(Icons.Rounded.LocalParking, "Parking", "À proximité", p) { go("parking") }
        DestinationRow(Icons.Rounded.ShoppingCart, "Supermarché", "À proximité", p) { go("supermarché") }
    }
}

@Composable
private fun DestinationRow(icon: ImageVector, title: String, detail: String, p: Palette, onClick: () -> Unit) {
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.hair))
        Row(
            modifier = Modifier.fillMaxWidth().height(62.dp).clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(p.pill), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = p.accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(title, color = p.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, color = p.sub, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(220.dp), textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun MusicSheet(p: Palette, now: Long, onClose: () -> Unit) {
    val context = LocalContext.current
    if (!MediaHub.granted) {
        SheetHeader("Musique", p, onClose = onClose)
        Spacer(Modifier.height(12.dp))
        Text(
            "Pour afficher le morceau en cours et le piloter, autorise Corsa Launcher dans « Accès aux notifications ».",
            color = p.text, fontSize = 16.sp,
        )
        Spacer(Modifier.height(16.dp))
        PillButton("Autoriser l'accès", p, selected = true) { Apps.openNotificationAccess(context) }
        return
    }
    val duration = MediaHub.durationMs
    val position = if (now > 0) MediaHub.positionMs() else 0L
    var dragging by remember { mutableStateOf<Float?>(null) }
    val fraction = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Row(Modifier.fillMaxWidth().height(330.dp)) {
        Artwork(p, 300.dp, radius = 18.dp)
        Spacer(Modifier.width(24.dp))
        Column(Modifier.weight(1f).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            SheetHeader(
                title = MediaHub.appName ?: "Musique",
                p = p,
                subtitle = if (MediaHub.title == null) "Aucune lecture en cours" else null,
                onClose = onClose,
            )
            Column {
                Text(MediaHub.title ?: "—", color = p.text, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(MediaHub.artist ?: "", color = p.sub, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column {
                Slider(
                    value = dragging ?: fraction,
                    onValueChange = { dragging = it },
                    onValueChangeFinished = {
                        val d = dragging
                        if (d != null && duration > 0) MediaHub.seekTo((d * duration).toLong())
                        dragging = null
                    },
                    enabled = duration > 0,
                    colors = SliderDefaults.colors(thumbColor = p.text, activeTrackColor = p.text, inactiveTrackColor = p.pill),
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(formatTime(position), color = p.sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text(if (duration > 0) formatTime(duration) else "", color = p.sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconBtn(Icons.Rounded.SkipPrevious, "Précédent", p, size = 64.dp, iconSize = 42.dp) { MediaHub.prev() }
                Spacer(Modifier.width(24.dp))
                IconBtn(
                    if (MediaHub.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (MediaHub.playing) "Pause" else "Lecture",
                    p, size = 76.dp, iconSize = 44.dp, background = p.text, tint = p.inv,
                ) { MediaHub.playPause() }
                Spacer(Modifier.width(24.dp))
                IconBtn(Icons.Rounded.SkipNext, "Suivant", p, size = 64.dp, iconSize = 42.dp) { MediaHub.next() }
                Spacer(Modifier.weight(1f))
                val pkg = MediaHub.pkg
                if (pkg != null) {
                    PillButton("Ouvrir l'appli", p) {
                        if (!Apps.launch(context, pkg)) Apps.toast(context, "Impossible d'ouvrir l'appli")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSheet(p: Palette, prefs: Prefs, sunrise: Long, sunset: Long, onClose: () -> Unit) {
    val context = LocalContext.current
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.FRANCE) }
    val version = remember {
        try {
            @Suppress("DEPRECATION")
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: "?"
        } catch (e: Exception) {
            "?"
        }
    }
    SheetHeader("Réglages", p, subtitle = "Version $version", onClose = onClose)
    Spacer(Modifier.height(12.dp))
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Label("Thème", p)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton("Nuit", p, Modifier.weight(1f), selected = prefs.theme == "nuit") { prefs.theme = "nuit" }
            PillButton("Jour", p, Modifier.weight(1f), selected = prefs.theme == "jour") { prefs.theme = "jour" }
            PillButton("Auto", p, Modifier.weight(1f), selected = prefs.theme == "auto") { prefs.theme = "auto" }
        }
        Text(
            "Auto suit le soleil à ta position : lever ${fmt.format(Date(sunrise))}, coucher ${fmt.format(Date(sunset))}." +
                if (LocationHub.lat == null) " Position GPS pas encore reçue : calcul pour Tubize." else "",
            color = p.sub, fontSize = 14.sp,
        )

        Label("Appli de navigation", p)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton(
                "Waze" + if (Apps.isInstalled(context, Apps.WAZE)) "" else " (non installée)",
                p, Modifier.weight(1f), selected = prefs.navApp == "waze",
            ) { prefs.navApp = "waze" }
            PillButton(
                "Google Maps" + if (Apps.isInstalled(context, Apps.GOOGLE_MAPS)) "" else " (non installée)",
                p, Modifier.weight(1f), selected = prefs.navApp == "maps",
            ) { prefs.navApp = "maps" }
        }

        Label("Adresses favorites", p)
        OutlinedTextField(
            value = prefs.home,
            onValueChange = { prefs.home = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Domicile") },
            colors = fieldColors(p),
        )
        OutlinedTextField(
            value = prefs.work,
            onValueChange = { prefs.work = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Travail") },
            colors = fieldColors(p),
        )

        Label("Autoradio", p)
        PillButton("Choisir l'écran d'accueil par défaut", p, Modifier.fillMaxWidth()) { Apps.openHomeSettings(context) }
        PillButton(
            if (MediaHub.granted) "Accès à la musique : autorisé" else "Autoriser l'accès à la musique",
            p, Modifier.fillMaxWidth(), selected = !MediaHub.granted,
        ) { Apps.openNotificationAccess(context) }
        PillButton("Paramètres Android", p, Modifier.fillMaxWidth()) { Apps.openAndroidSettings(context) }
        Spacer(Modifier.height(4.dp))
        Text("Carte © OpenFreeMap, © OpenMapTiles, données © contributeurs OpenStreetMap", color = p.sub, fontSize = 12.sp)
    }
}

@Composable
private fun VehicleSheet(p: Palette, onClose: () -> Unit) {
    SheetHeader("Corsa D", p, subtitle = "Ce qui est déjà disponible et ce qui arrive", onClose = onClose)
    Spacer(Modifier.height(12.dp))
    val speed = LocationHub.speedKmh
    Column(Modifier.verticalScroll(rememberScrollState())) {
        VehicleRow("Vitesse", if (speed != null) "$speed km/h · GPS" else "En attente du GPS", p, available = speed != null)
        VehicleRow("Conso, régime, température, batterie", "Boîtier OBD2 Bluetooth à brancher", p, available = false)
        VehicleRow("Portières et hayon", "Boîtier CANbus fourni : à tester", p, available = false)
        VehicleRow("Pression des pneus", "Capteurs de valve à acheter", p, available = false)
        VehicleRow("Suivi des pleins", "Prochaine version", p, available = false)
    }
}

@Composable
private fun VehicleRow(title: String, detail: String, p: Palette, available: Boolean) {
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.hair))
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(if (available) p.ok else p.sub))
            Spacer(Modifier.width(14.dp))
            Text(title, color = p.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(detail, color = if (available) p.ok else p.sub, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun Label(text: String, p: Palette) {
    Text(text, color = p.sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun fieldColors(p: Palette) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = p.text,
    unfocusedTextColor = p.text,
    focusedBorderColor = p.accent,
    unfocusedBorderColor = p.hair,
    focusedLabelColor = p.accent,
    unfocusedLabelColor = p.sub,
    cursorColor = p.accent,
    focusedPlaceholderColor = p.sub,
    unfocusedPlaceholderColor = p.sub,
)
