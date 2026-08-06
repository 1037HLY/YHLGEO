package com.geosurvey.toolbox.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.geosurvey.toolbox.presentation.theme.GeoSurveyTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var locationHelper: LocationHelper

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化定位助手
        locationHelper = LocationHelper(this)

        setContent {
            GeoSurveyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8FAFC)
                ) {
                    val permissionsState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
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

                    MainScreen(
                        hasPermission = hasPermission,
                        onRequestPermission = {
                            permissionsState.launchMultiplePermissionRequest()
                        },
                        locationHelper = locationHelper
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdates()
    }
}

@Composable
fun MainScreen(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    locationHelper: LocationHelper
) {
    // 定位数据状态
    var location by remember { mutableStateOf<Location?>(null) }
    var isSearching by remember { mutableStateOf(true) }
    var satelliteCount by remember { mutableStateOf(0) }
    var timeText by remember { mutableStateOf("--:--:--") }

    // 开始监听GPS数据
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            locationHelper.startLocationUpdates(
                onLocationUpdate = { newLocation ->
                    location = newLocation
                    isSearching = false
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    timeText = timeFormat.format(Date())
                },
                onSatelliteUpdate = { count ->
                    satelliteCount = count
                }
            )
        }
    }

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

        // 权限状态提示
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

        // 定位卡片 - 显示真实GPS数据
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
                    Text(
                        text = "经度: %.6f  纬度: %.6f".format(
                            location!!.longitude,
                            location!!.latitude
                        ),
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "海拔: %.1fm  速度: %.1fkm/h".format(
                            location!!.altitude,
                            location!!.speed * 3.6
                        ),
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "精度: %.1fm  🛰️ 卫星: $satelliteCount 颗",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🕐 $timeText",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
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
                    // 显示搜索时间
                    if (hasPermission && isSearching) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⏳ 首次定位可能需要30-60秒",
                            fontSize = 12.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
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
                Text(
                    text = "🛣️ 轨迹记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (!isSearching && location != null) {
                        "📍 当前位置: %.4f, %.4f".format(location!!.latitude, location!!.longitude)
                    } else {
                        "⏳ 等待定位..."
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 产状测量卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F3FF).copy(alpha = 0.7f)
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
                    text = "阶段6开发中",
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
