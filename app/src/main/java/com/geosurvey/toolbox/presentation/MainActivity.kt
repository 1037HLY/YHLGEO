package com.geosurvey.toolbox.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geosurvey.toolbox.data.database.TrackPointEntity
import com.geosurvey.toolbox.presentation.theme.GeoSurveyTheme
import com.geosurvey.toolbox.presentation.ui.components.GnssFullScreenDialog
import com.geosurvey.toolbox.presentation.ui.components.TrackingCard
import com.geosurvey.toolbox.presentation.ui.screens.AnalysisScreen
import com.geosurvey.toolbox.presentation.ui.screens.AttitudeScreen
import com.geosurvey.toolbox.presentation.ui.screens.MapScreen
import com.geosurvey.toolbox.presentation.ui.screens.TrackListScreen
import com.geosurvey.toolbox.presentation.viewmodel.AnalysisViewModel
import com.geosurvey.toolbox.presentation.viewmodel.AttitudeViewModel
import com.geosurvey.toolbox.presentation.viewmodel.TrackingUiState
import com.geosurvey.toolbox.presentation.viewmodel.TrackingViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var locationHelper: LocationHelper
    private var trackingReceiver: BroadcastReceiver? = null

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationHelper = LocationHelper(this)

        trackingReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.geosurvey.toolbox.TRACKING_STATUS") {
                    // 状态会通过ViewModel更新
                }
            }
        }
        IntentFilter("com.geosurvey.toolbox.TRACKING_STATUS").also {
            registerReceiver(trackingReceiver, it, Context.RECEIVER_NOT_EXPORTED)
        }

        setContent {
            GeoSurveyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8FAFC)
                ) {
                    val permissionsState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )

                    var hasPermission by remember {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    }

                    LaunchedEffect(permissionsState.allPermissionsGranted) {
                        hasPermission = permissionsState.allPermissionsGranted
                    }

                    LaunchedEffect(Unit) {
                        if (!hasPermission) {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }

                    var location by remember { mutableStateOf<Location?>(null) }
                    var isSearching by remember { mutableStateOf(true) }
                    var gnssData by remember { mutableStateOf(GnssStatusData(emptyList(), 0, 0, 0f, 0f, 0f)) }
                    var timeText by remember { mutableStateOf("--:--:--") }
                    var showDialog by remember { mutableStateOf(false) }
                    var showTrackList by remember { mutableStateOf(false) }
                    var showMap by remember { mutableStateOf(false) }
                    var showAttitude by remember { mutableStateOf(false) }
                    var showAnalysis by remember { mutableStateOf(false) }

                    LaunchedEffect(hasPermission) {
                        if (hasPermission) {
                            locationHelper.startLocationUpdates(
                                onLocationUpdate = { newLocation ->
                                    location = newLocation
                                    isSearching = false
                                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    timeText = timeFormat.format(Date())
                                },
                                onGnssStatusUpdate = { data ->
                                    gnssData = data
                                }
                            )
                        }
                    }

                    val trackingViewModel: TrackingViewModel = viewModel()
                    val trackingState by trackingViewModel.uiState.collectAsState()

                    // 页面切换
                    if (showAnalysis) {
                        val analysisViewModel: AnalysisViewModel = viewModel()
                        AnalysisScreen(
                            viewModel = analysisViewModel,
                            onBack = { showAnalysis = false }
                        )
                    } else if (showAttitude) {
                        val attitudeViewModel: AttitudeViewModel = viewModel()
                        LaunchedEffect(location) {
                            location?.let {
                                attitudeViewModel.updateLocation(it)
                            }
                        }
                        AttitudeScreen(
                            viewModel = attitudeViewModel,
                            onBack = { showAttitude = false }
                        )
                    } else if (showMap) {
                        val selectedTrackId = trackingState.trackList.firstOrNull()?.trackId
                        val trackPoints = if (selectedTrackId != null) {
                            remember(selectedTrackId) {
                                emptyList<TrackPointEntity>()
                            }
                        } else {
                            emptyList<TrackPointEntity>()
                        }
                        MapScreen(
                            trackPoints = trackPoints,
                            currentLocation = location,
                            onBack = { showMap = false },
                            onCenterLocation = {
                                // 定位到当前位置
                            }
                        )
                    } else if (showTrackList) {
                        TrackListScreen(
                            tracks = trackingState.trackList,
                            onTrackClick = { trackId ->
                                showMap = true
                            },
                            onDeleteTrack = { trackId ->
                                trackingViewModel.deleteTrack(trackId)
                            },
                            onBack = { showTrackList = false }
                        )
                    } else {
                        MainScreen(
                            hasPermission = hasPermission,
                            onRequestPermission = {
                                permissionsState.launchMultiplePermissionRequest()
                            },
                            location = location,
                            isSearching = isSearching,
                            gnssData = gnssData,
                            timeText = timeText,
                            onCardClick = {
                                if (!isSearching && location != null) {
                                    showDialog = true
                                }
                            },
                            trackingState = trackingState,
                            onStartTracking = {
                                trackingViewModel.startRecording()
                            },
                            onStopTracking = {
                                trackingViewModel.stopRecording()
                            },
                            onViewTracks = {
                                trackingViewModel.loadAllTracks()
                                showTrackList = true
                            },
                            onMapClick = {
                                showMap = true
                            },
                            onAttitudeClick = {
                                showAttitude = true
                            },
                            onAnalysisClick = {
                                showAnalysis = true
                            }
                        )
                    }

                    if (showDialog) {
                        GnssFullScreenDialog(
                            statusData = gnssData,
                            onDismiss = { showDialog = false }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdates()
        trackingReceiver?.let { unregisterReceiver(it) }
    }
}

@Composable
fun MainScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    location: Location?,
    isSearching: Boolean,
    gnssData: GnssStatusData,
    timeText: String,
    onCardClick: () -> Unit,
    trackingState: TrackingUiState,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onViewTracks: () -> Unit,
    onMapClick: () -> Unit,
    onAttitudeClick: () -> Unit,
    onAnalysisClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏔️ 地质勘查工具箱",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "GeoSurvey Toolbox v1.0.0",
            fontSize = 16.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!hasPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ 需要定位权限",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0EA5E9)
                        )
                    ) {
                        Text("授予权限")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 定位卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F0FE).copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📍 GPS定位",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0EA5E9)
                    )
                    Text(
                        text = if (isSearching) "🔍 搜索中..." else "✅ 已定位",
                        fontSize = 14.sp,
                        color = if (isSearching) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isSearching && location != null) {
                    val loc = location!!
                    Text(
                        text = "经度: %.6f  纬度: %.6f".format(loc.longitude, loc.latitude),
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "海拔: %.1fm  速度: %.1fkm/h".format(loc.altitude, loc.speed * 3.6),
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("精度: %.1fm  🛰️ 卫星: %d 颗", loc.accuracy, gnssData.totalCount),
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🕐 $timeText",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    if (gnssData.totalCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "👆 点击查看卫星详情",
                            fontSize = 12.sp,
                            color = Color(0xFF0EA5E9)
                        )
                    }
                } else {
                    Text(
                        text = "🛰️ 正在搜索卫星信号...",
                        fontSize = 14.sp,
                        color = Color(0xFF0EA5E9)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "请确保GPS已开启，并移动到开阔地带",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 轨迹记录卡片
        TrackingCard(
            isRecording = trackingState.isRecording,
            pointCount = trackingState.pointCount,
            onStartClick = onStartTracking,
            onStopClick = onStopTracking,
            onViewTracksClick = onViewTracks
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 地图卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMapClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F0FE).copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "🗺️ 轨迹地图",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0EA5E9)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "查看轨迹路线和当前位置",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
                if (trackingState.trackList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📋 已有 ${trackingState.trackList.size} 条轨迹",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 产状测量卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAttitudeClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F3FF).copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "🔬 产状测量",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8B5CF6)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "测量岩层走向/倾角/倾向",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 分析工具卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAnalysisClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7).copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "📊 地质分析",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "赤平投影 · 玫瑰花图 · 统计分析",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (!hasPermission) {
                "⏳ 请授予定位权限以使用GPS功能"
            } else if (isSearching) {
                "🔍 正在搜索GPS信号... 请稍候"
            } else {
                "✅ 已获取真实GPS定位数据"
            },
            fontSize = 14.sp,
            color = if (isSearching && hasPermission) Color(0xFFF59E0B) else Color(0xFF10B981)
        )
    }
}
