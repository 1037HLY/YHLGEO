package com.geosurvey.toolbox.utils

import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

object MapUtils {
    
    fun initOsmdroid(context: Context) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        // 设置缓存目录
        Configuration.getInstance().osmdroidBasePath = File(
            context.cacheDir.absolutePath,
            "osmdroid"
        )
        Configuration.getInstance().osmdroidTileCache = File(
            context.cacheDir.absolutePath,
            "osmdroid/tiles"
        )
    }

    fun createMapView(context: Context): MapView {
        return MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setBuiltInZoomControls(true)
            setMultiTouchControls(true)
            isHorizontalMapRepetitionEnabled = false
        }
    }

    fun createMarker(
        mapView: MapView,
        position: GeoPoint,
        title: String,
        snippet: String = ""
    ): Marker {
        return Marker(mapView).apply {
            setPosition(position)
            setTitle(title)
            setSnippet(snippet)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
    }

    fun createPolyline(
        points: List<GeoPoint>,
        color: Int = 0xFF0EA5E9.toInt(),
        width: Float = 5f
    ): Polyline {
        return Polyline().apply {
            setPoints(points)
            outlinePaint.color = color
            outlinePaint.strokeWidth = width
        }
    }

    fun geoPointToLatLng(point: GeoPoint): Pair<Double, Double> {
        return Pair(point.latitude, point.longitude)
    }

    fun latLngToGeoPoint(lat: Double, lng: Double): GeoPoint {
        return GeoPoint(lat, lng)
    }

    fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0].toDouble()
    }

    fun calculateTotalDistance(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += calculateDistance(points[i], points[i + 1])
        }
        return total
    }
}
