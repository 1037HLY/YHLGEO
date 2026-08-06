package com.geosurvey.toolbox.presentation.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.geosurvey.toolbox.utils.MapUtils
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint? = null,
    zoom: Double = 15.0,
    markers: List<MarkerData> = emptyList(),
    polylines: List<PolylineData> = emptyList(),
    onMapReady: (MapView) -> Unit = {},
    onMapClick: (GeoPoint) -> Unit = {}
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        MapUtils.initOsmdroid(context)
        onDispose { }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setBuiltInZoomControls(true)
                setMultiTouchControls(true)
                isHorizontalMapRepetitionEnabled = false

                // 设置中心点
                center?.let {
                    controller.setCenter(it)
                    controller.setZoom(zoom)
                }

                // 添加覆盖层
                markers.forEach { markerData ->
                    val marker = MapUtils.createMarker(
                        this,
                        markerData.position,
                        markerData.title,
                        markerData.snippet
                    )
                    markerData.icon?.let { icon ->
                        marker.icon = icon
                    }
                    overlays.add(marker)
                }

                polylines.forEach { polylineData ->
                    val polyline = MapUtils.createPolyline(
                        polylineData.points,
                        polylineData.color,
                        polylineData.width
                    )
                    overlays.add(polyline)
                }

                onMapReady(this)
            }
        },
        update = { mapView ->
            // 更新中心点
            center?.let {
                mapView.controller.setCenter(it)
            }
            // 更新缩放
            mapView.controller.setZoom(zoom)

            // 清除旧覆盖层，保留基础层
            mapView.overlays.clear()

            // 重新添加标记
            markers.forEach { markerData ->
                val marker = MapUtils.createMarker(
                    mapView,
                    markerData.position,
                    markerData.title,
                    markerData.snippet
                )
                markerData.icon?.let { icon ->
                    marker.icon = icon
                }
                mapView.overlays.add(marker)
            }

            // 重新添加轨迹
            polylines.forEach { polylineData ->
                val polyline = MapUtils.createPolyline(
                    polylineData.points,
                    polylineData.color,
                    polylineData.width
                )
                mapView.overlays.add(polyline)
            }

            mapView.invalidate()
        }
    )
}

data class MarkerData(
    val position: GeoPoint,
    val title: String,
    val snippet: String = "",
    val icon: Bitmap? = null
)

data class PolylineData(
    val points: List<GeoPoint>,
    val color: Int = 0xFF0EA5E9.toInt(),
    val width: Float = 5f
)
