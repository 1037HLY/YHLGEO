package com.geosurvey.toolbox.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.geosurvey.toolbox.presentation.viewmodel.AttitudeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttitudeScreen(
    viewModel: AttitudeViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 检查定位权限
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color(0xFF475569)
                    )
                }
                Text(
                    text = "🔬 产状测量",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
            Text(
                text = if (uiState.isMeasuring) "📡 测量中" else "⏸️ 已暂停",
                fontSize = 14.sp,
                color = if (uiState.isMeasuring) Color(0xFF10B981) else Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 产状数值显示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F3FF).copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 走向、倾角、倾向三列
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 走向
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "走向",
                            fontSize = 14.sp,
                            color = Color(0xFF8B5CF6)
                        )
                        Text(
                            text = String.format("%.1f°", uiState.strike),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // 倾角
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "倾角",
                            fontSize = 14.sp,
                            color = Color(0xFF8B5CF6)
                        )
                        Text(
                            text = String.format("%.1f°", uiState.dip),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // 倾向
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "倾向",
                            fontSize = 14.sp,
                            color = Color(0xFF8B5CF6)
                        )
                        Text(
                            text = String.format("%.1f°", uiState.dipDirection),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 测量提示
                Text(
                    text = "📱 将手机背面贴合岩面进行测量",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )

                // 精度指示
                if (uiState.accuracy > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "精度: ${uiState.accuracy}%",
                        fontSize = 12.sp,
                        color = if (uiState.accuracy > 80) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 备注输入
        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.updateNote(it) },
            label = { Text("📝 备注") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedBorderColor = Color(0xFFD1D5DB)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 位置信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F0FE).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "📍 位置信息",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0EA5E9)
                )
                if (hasLocationPermission && uiState.currentLocation != null) {
                    val loc = uiState.currentLocation!!
                    Text(
                        text = "经度: %.6f  纬度: %.6f".format(loc.longitude, loc.latitude),
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "海拔: %.1fm".format(loc.altitude),
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                } else {
                    Text(
                        text = if (hasLocationPermission) "⏳ 等待定位..." else "⚠️ 请授予定位权限",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 记录按钮
        Button(
            onClick = { viewModel.saveAttitude() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "📝 记录产状",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 历史记录标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 历史记录 (${uiState.history.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            TextButton(
                onClick = { viewModel.loadHistory() }
            ) {
                Text("刷新", fontSize = 12.sp, color = Color(0xFF8B5CF6))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 历史记录列表
        if (uiState.history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无产状记录\n\n将手机贴合岩面测量并记录",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(uiState.history) { record ->
                    AttitudeHistoryItem(
                        record = record,
                        onDelete = { viewModel.deleteAttitude(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AttitudeHistoryItem(
    record: com.geosurvey.toolbox.data.database.AttitudeEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 产状数值行
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "走向: %.1f°".format(record.strike),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "倾角: %.1f°".format(record.dip),
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "倾向: %.1f°".format(record.dipDirection),
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 坐标
                Text(
                    text = "📍 %.5f, %.5f".format(record.latitude, record.longitude),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                // 备注
                if (record.note.isNotEmpty()) {
                    Text(
                        text = "📝 ${record.note}",
                        fontSize = 11.sp,
                        color = Color(0xFF8B5CF6)
                    )
                }

                // 时间
                Text(
                    text = "🕐 ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(record.timestamp))}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
