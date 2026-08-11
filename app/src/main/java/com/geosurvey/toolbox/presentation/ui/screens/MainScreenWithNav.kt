package com.geosurvey.toolbox.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.presentation.GnssStatusData
import com.geosurvey.toolbox.presentation.viewmodel.TrackingUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithNav(
    // GPS定位相关
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    location: android.location.Location?,
    isSearching: Boolean,
    gnssData: GnssStatusData,
    timeText: String,
    onCardClick: () -> Unit,
    // 轨迹记录相关
    trackingState: TrackingUiState,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onViewTracks: () -> Unit,
    // 产状测量
    onAttitudeClick: () -> Unit,
    // 地质分析
    onAnalysisClick: () -> Unit,
    // 样本记录
    onSampleClick: () -> Unit,
    // 水印相机
    onCameraPageClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF8FAFC),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 0) Icons.Filled.LocationOn else Icons.Outlined.LocationOn,
                            contentDescription = "定位与轨迹"
                        )
                    },
                    label = { Text("定位与轨迹", fontSize = 10.sp) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 1) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "产状与分析"
                        )
                    },
                    label = { Text("产状与分析", fontSize = 10.sp) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 2) Icons.Filled.List else Icons.Outlined.List,
                            contentDescription = "样本记录"
                        )
                    },
                    label = { Text("样本记录", fontSize = 10.sp) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 3) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                            contentDescription = "水印相机"
                        )
                    },
                    label = { Text("水印相机", fontSize = 10.sp) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> LocationAndTrackScreen(
                    hasPermission = hasPermission,
                    onRequestPermission = onRequestPermission,
                    location = location,
                    isSearching = isSearching,
                    gnssData = gnssData,
                    timeText = timeText,
                    onCardClick = onCardClick,
                    trackingState = trackingState,
                    onStartTracking = onStartTracking,
                    onStopTracking = onStopTracking,
                    onViewTracks = onViewTracks
                )
                1 -> AttitudeAndAnalysisScreen(
                    onAttitudeClick = onAttitudeClick,
                    onAnalysisClick = onAnalysisClick
                )
                2 -> SampleScreenPlaceholder(onSampleClick = onSampleClick)
                3 -> CameraScreenPlaceholder(onCameraClick = onCameraPageClick)
            }
        }
    }
}

@Composable
fun LocationAndTrackScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    location: android.location.Location?,
    isSearching: Boolean,
    gnssData: GnssStatusData,
    timeText: String,
    onCardClick: () -> Unit,
    trackingState: TrackingUiState,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onViewTracks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // GPS定位卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFECFDF5).copy(alpha = 0.7f)
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
                        text = "🛣️ 轨迹记录",
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                    if (trackingState.isRecording) {
                        Text(
                            text = "● 记录中",
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = if (trackingState.isRecording) onStopTracking else onStartTracking,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (trackingState.isRecording) Color(0xFFEF4444) else Color(0xFF10B981)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (trackingState.isRecording) "停止记录" else "开始记录")
                    }
                    Button(
                        onClick = onViewTracks,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0EA5E9)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("历史轨迹")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "已记录: ${trackingState.pointCount} 个点",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
fun AttitudeAndAnalysisScreen(
    onAttitudeClick: () -> Unit,
    onAnalysisClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 产状测量卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F3FF).copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clickable { onAttitudeClick() },
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔬 产状测量",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = Color(0xFF8B5CF6)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "测量岩层走向/倾角/倾向",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 地质分析卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7).copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clickable { onAnalysisClick() },
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📊 地质分析",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "赤平投影 · 玫瑰花图 · 统计分析",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
fun SampleScreenPlaceholder(onSampleClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { onSampleClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD1FAE5).copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📋 样本记录",
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF059669)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击进入样本管理",
                    fontSize = 16.sp,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "普通样本 · 钻孔样本 · 导出数据",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun CameraScreenPlaceholder(onCameraClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { onCameraClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E0).copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📷 水印相机",
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击进入相机",
                    fontSize = 16.sp,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "拍照并叠加坐标/时间/产状水印",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
