package be.corsa.launcher.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.OnCameraTrackingChangedListener
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/** Pilote la carte depuis les boutons de l'interface. */
class MapController {
    var map: MapLibreMap? = null
    var tracking by mutableStateOf(true)
    var locationActive = false
    var listenerAdded = false
    var centeredOnce = false

    fun zoomIn() {
        map?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    fun zoomOut() {
        map?.animateCamera(CameraUpdateFactory.zoomOut())
    }

    @SuppressLint("MissingPermission")
    fun recenter() {
        val m = map ?: return
        if (!locationActive) return
        try {
            m.locationComponent.cameraMode = CameraMode.TRACKING_GPS
            m.animateCamera(CameraUpdateFactory.zoomTo(16.0))
            tracking = true
        } catch (e: Exception) {
            tracking = false
        }
    }
}

/**
 * Carte MapLibre : se déplace au doigt, se zoome en écartant deux doigts,
 * et suit la voiture tant qu'on ne la déplace pas.
 */
@Composable
fun MapPane(modifier: Modifier, p: Palette, styleUrl: String, locationGranted: Boolean, ctrl: MapController) {
    if (LocalInspectionMode.current) {
        MapPreview(p, modifier)
        return
    }
    val context = LocalContext.current
    val lifecycle = (context as LifecycleOwner).lifecycle
    val mapView = remember { MapView(context).apply { onCreate(null) } }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(styleUrl, locationGranted) {
        mapView.getMapAsync { map ->
            ctrl.map = map
            if (!ctrl.centeredOnce) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(50.69, 4.20), 14.0))
                ctrl.centeredOnce = true
            }
            map.setStyle(styleUrl) { style ->
                if (locationGranted) enableLocation(context, map, style, ctrl)
            }
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

@SuppressLint("MissingPermission")
private fun enableLocation(context: Context, map: MapLibreMap, style: Style, ctrl: MapController) {
    try {
        val lc = map.locationComponent
        lc.activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build())
        lc.isLocationComponentEnabled = true
        lc.renderMode = RenderMode.GPS
        if (!ctrl.listenerAdded) {
            lc.addOnCameraTrackingChangedListener(object : OnCameraTrackingChangedListener {
                override fun onCameraTrackingDismissed() {
                    ctrl.tracking = false
                }

                override fun onCameraTrackingChanged(currentMode: Int) {
                    ctrl.tracking = currentMode != CameraMode.NONE
                }
            })
            ctrl.listenerAdded = true
        }
        ctrl.locationActive = true
        if (ctrl.tracking) {
            lc.cameraMode = CameraMode.TRACKING_GPS
            map.animateCamera(CameraUpdateFactory.zoomTo(16.0))
        }
    } catch (e: Exception) {
        ctrl.locationActive = false
    }
}
