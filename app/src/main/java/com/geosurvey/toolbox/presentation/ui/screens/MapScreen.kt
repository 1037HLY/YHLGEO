package com.geosurvey.toolbox.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.data.database.TrackPointEntity
import com.geosurvey.toolbox.presentation.ui.components.OsmMapView
import com.geosurvey.toolbox.presentation.ui.components.PolylineData
import com.geosurvey.toolbox.utils.MapUtils
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    trackPoints: List<TrackPointEntity>,
    currentLocation: android.location.Location?,
    onBack: () -> Unit,
    onCenterLocation: () -> Unit
) {
    val context = LocalContext.current
    var mapZoom by remember { mutableStateOf(15.0) }
    var centerPoint by remember { mutableStateOf<GeoPoint?>(null) }

    // 计算轨迹中心点
    val trackCenter = remember(trackPoints) {
        if (trackPoints.isNotEmpty()) {
            val avgLat = trackPoints.map { it.latitude }.average()
            val avgLng = trackPoints.map { it.longitude }.average()
            GeoPoint(avgLat, avgLng)
        } else {
            currentLocation?.let {
                GeoPoint(it.latitude, it.longitude)
            }
        }
    }

    // 更新中心点
    LaunchedEffect(trackCenter) {
        centerPoint = trackCenter
    }

    // 转换轨迹点为GeoPoint
    val geoPoints = remember(trackPoints) {
        trackPoints.map { GeoPoint(it.latitude, it.longitude) }
    }

    // 构建轨迹线数据
    val polylines = remember(geoPoints) {
        if (geoPoints.isNotEmpty()) {
            listOf(
                PolylineData(
                    points = geoPoints,
                    color = 0xFF0EA5E9.toInt(),
                    width = 6f
                )
            )
        } else {
            emptyList()
        }
    }

    // 当前位置标记
    val currentGeoPoint = currentLocation?.let {
        GeoPoint(it.latitude, it.longitude)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部工具栏
        TopAppBar(
            title = {
                Text(
                    text = "🗺️ 轨迹地图",
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color(0xFF0F172A)
                    )
                }
            },
            actions = {
                IconButton(onClick = onCenterLocation) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "定位到当前位置",
                        tint = Color(0xFF0EA5E9)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF8FAFC)
            )
        )

        // 地图
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                center = centerPoint ?: currentGeoPoint,
                zoom = mapZoom,
                polylines = polylines,
                onMapReady = { mapView ->
                    // 地图准备就绪
                }
            )

            // 地图信息浮层
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFC).copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📊 轨迹信息",
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📍 点数: ${trackPoints.size}",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        if (geoPoints.size > 1) {
                            val distance = MapUtils.calculateTotalDistance(geoPoints)
                            Text(
                                text = "📏 距离: ${String.format("%.2f", distance / 1000)} km",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                        if (trackPoints.isNotEmpty()) {
                            val startTime = trackPoints.firstOrNull()?.timestamp
                            val endTime = trackPoints.lastOrNull()?.timestamp
                            if (startTime != null && endTime != null) {
                                val duration = (endTime - startTime) / 1000 / 60
                                Text(
                                    text = "⏱️ ${duration} 分钟",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
