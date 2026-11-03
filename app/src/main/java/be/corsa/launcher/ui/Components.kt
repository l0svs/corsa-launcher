package be.corsa.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.corsa.launcher.Apps
import be.corsa.launcher.MediaHub

/** Carte blanche (ou sombre) arrondie avec un fin contour. */
fun Modifier.card(p: Palette, radius: Dp = 18.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return this.clip(shape).background(p.card).border(1.dp, p.hair, shape)
}

/** Empêche les touches de traverser un panneau et de faire bouger la carte en dessous. */
fun Modifier.blockTouches(): Modifier = this.pointerInput(Unit) { detectTapGestures { } }

@Composable
fun IconBtn(
    icon: ImageVector,
    description: String,
    p: Palette,
    size: Dp = 52.dp,
    iconSize: Dp = 28.dp,
    background: Color = Color.Transparent,
    tint: Color = p.text,
    shape: Shape = CircleShape,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Bouton rond à contour, pour les commandes posées sur la carte. */
@Composable
fun MapButton(icon: ImageVector, description: String, p: Palette, tint: Color = p.text, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(p.card)
            .border(1.dp, p.hair, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(26.dp))
    }
}

/** Bouton texte : plein (sélectionné) ou discret. */
@Composable
fun PillButton(
    label: String,
    p: Palette,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: ImageVector? = null,
    image: ImageBitmap? = null,
    textColor: Color? = null,
    onClick: () -> Unit,
) {
    val bg = if (selected) p.text else p.pill
    val fg = textColor ?: if (selected) p.inv else p.text
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        if (image != null) {
            Image(bitmap = image, contentDescription = null, modifier = Modifier.size(26.dp).clip(RoundedCornerShape(7.dp)))
            androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        }
        Text(label, color = fg, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun SheetHeader(title: String, p: Palette, subtitle: String? = null, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(title, color = p.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = p.sub, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        IconBtn(Icons.Rounded.Close, "Fermer", p, size = 46.dp, iconSize = 24.dp, background = p.pill, onClick = onClose)
    }
}

/** Vraie icône d'une appli installée sur l'autoradio (null si absente). */
@Composable
fun rememberAppIcon(pkg: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(pkg) { pkg?.let { Apps.icon(context, it) } }
}

/**
 * Pochette du morceau en cours, avec l'icône de l'appli qui joue (Spotify, Apple Music…) en badge.
 * Sans pochette, l'icône de l'appli prend toute la place.
 */
@Composable
fun Artwork(p: Palette, size: Dp, radius: Dp = 12.dp) {
    val bitmap = MediaHub.art
    val image = remember(bitmap) { bitmap?.asImageBitmap() }
    val appIcon = rememberAppIcon(MediaHub.pkg)
    val shape = RoundedCornerShape(radius)
    Box(Modifier.size(size)) {
        when {
            image != null -> Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape),
            )
            appIcon != null -> Box(
                modifier = Modifier.fillMaxSize().clip(shape).background(p.pill),
                contentAlignment = Alignment.Center,
            ) {
                Image(bitmap = appIcon, contentDescription = null, modifier = Modifier.fillMaxSize(0.72f))
            }
            else -> Box(
                modifier = Modifier.fillMaxSize().clip(shape).background(p.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(size / 2))
            }
        }
        if (image != null && appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.34f)
                    .clip(CircleShape)
                    .border(2.dp, p.card, CircleShape),
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
}
