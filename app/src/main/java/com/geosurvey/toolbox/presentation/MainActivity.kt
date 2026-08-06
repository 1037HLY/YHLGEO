package com.geosurvey.toolbox.presentation

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geosurvey.toolbox.data.repository.LocationRepository
import com.geosurvey.toolbox.domain.usecase.GetLocationUseCase
import com.geosurvey.toolbox.presentation.theme.GeoSurveyTheme
import com.geosurvey.toolbox.presentation.ui.components.LocationCard
import com.geosurvey.toolbox.presentation.viewmodel.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeoSurveyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8FAFC)
                ) {
                    // 权限请求
                    val permissionsState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )

                    // 在首次启动时请求权限
                    LaunchedEffect(Unit) {
                        if (!permissionsState.allPermissionsGranted) {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }

                    // 检查是否有定位权限
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    // 创建ViewModel - 只有有权限时才创建
                    val viewModel = if (hasPermission) {
                        try {
                            val repository = LocationRepository(applicationContext)
                            val useCase = GetLocationUseCase(repository)
                            LocationViewModel(useCase)
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }

                    MainScreen(
                        viewModel = viewModel,
                        hasPermission = hasPermission,
                        onRequestPermission = {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: LocationViewModel?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val uiState = viewModel?.uiState?.collectAsState()

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

        // 定位卡片
        if (viewModel != null && hasPermission) {
            val state = uiState?.value
            LocationCard(
                location = state?.location,
                satelliteCount = state?.satelliteCount ?: 0,
                qualityText = state?.qualityText ?: "等待定位...",
                modifier = Modifier
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "📍 GPS定位",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0EA5E9)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (hasPermission) "等待卫星信号..." else "请先授予定位权限",
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 轨迹记录卡片（占位）
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFECFDF5).copy(alpha = 0.6f)
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
                    text = "未开始记录 (阶段3开发中)",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 产状测量卡片（占位）
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "准备测量 (阶段6开发中)",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "🔧 阶段2开发中... 正在获取定位数据",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8)
        )
    }
}
