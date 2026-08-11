package com.geosurvey.toolbox.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geosurvey.toolbox.data.database.TrackPointEntity
import com.geosurvey.toolbox.presentation.theme.GeoSurveyTheme
import com.geosurvey.toolbox.presentation.ui.components.GnssFullScreenDialog
import com.geosurvey.toolbox.presentation.ui.screens.AnalysisScreen
import com.geosurvey.toolbox.presentation.ui.screens.AttitudeScreen
import com.geosurvey.toolbox.presentation.ui.screens.CameraPageScreen
import com.geosurvey.toolbox.presentation.ui.screens.MainScreenWithNav
import com.geosurvey.toolbox.presentation.ui.screens.MapScreen
import com.geosurvey.toolbox.presentation.ui.screens.SampleScreenV2
import com.geosurvey.toolbox.presentation.ui.screens.SensorScreen
import com.geosurvey.toolbox.presentation.ui.screens.TrackListScreen
import com.geosurvey.toolbox.presentation.viewmodel.AnalysisViewModel
import com.geosurvey.toolbox.presentation.viewmodel.AttitudeViewModel
import com.geosurvey.toolbox.presentation.viewmodel.SampleViewModel
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

        // 启用全屏手势支持（边缘滑动返回）
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        // 主动请求权限（首次打开直接弹出权限对话框）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
            val needRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (needRequest.isNotEmpty()) {
                requestPermissions(needRequest.toTypedArray(), 100)
            }
        }

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
                    var showSample by remember { mutableStateOf(false) }
                    var showCameraPage by remember { mutableStateOf(false) }
                    var showSensor by remember { mutableStateOf(false) }

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
                    if (showSensor) {
                        SensorScreen()
                    } else if (showSample) {
                        val sampleViewModel: SampleViewModel = viewModel()
                        LaunchedEffect(location) {
                            location?.let { sampleViewModel.updateLocation(it) }
                        }
                        SampleScreenV2(
                            viewModel = sampleViewModel,
                            onBack = { showSample = false }
                        )
                    } else if (showCameraPage) {
                        CameraPageScreen(
                            onBack = { showCameraPage = false }
                        )
                    } else if (showAnalysis) {
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
                        val trackIds = trackingState.trackList.map { it.trackId }
                        val selectedTrackId = trackIds.firstOrNull()
                        var trackPoints by remember { mutableStateOf(emptyList<TrackPointEntity>()) }
                        
                        LaunchedEffect(selectedTrackId) {
                            if (selectedTrackId != null) {
                                try {
                                    trackPoints = trackingViewModel.getTrackPoints(selectedTrackId)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    trackPoints = emptyList()
                                }
                            } else {
                                trackPoints = emptyList()
                            }
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
                        // 主页面 - 底部导航
                        MainScreenWithNav(
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
                            onAttitudeClick = { showAttitude = true },
                            onAnalysisClick = { showAnalysis = true },
                            onSampleClick = { showSample = true },
                            onCameraPageClick = { showCameraPage = true },
                            onSensorClick = { showSensor = true }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            // 权限请求结果已由LaunchedEffect处理
        }
    }
}
