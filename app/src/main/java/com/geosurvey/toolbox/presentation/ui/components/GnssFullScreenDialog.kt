package com.geosurvey.toolbox.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.geosurvey.toolbox.presentation.GnssStatusData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GnssFullScreenDialog(
    statusData: GnssStatusData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛰️ GNSS卫星状态",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 统计信息
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("卫星总数", statusData.totalCount.toString())
                    StatItem("已用卫星", "${statusData.usedCount}")
                    StatItem("HDOP", String.format("%.1f", statusData.hdop))
                    StatItem("PDOP", String.format("%.1f", statusData.pdop))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 卫星极坐标图
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SatelliteChart(
                        satellites = statusData.satellites,
                        modifier = Modifier.size(280.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 卫星详情列表
                SatelliteDetailList(
                    satellites = statusData.satellites
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "📍 点击任意位置关闭",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0EA5E9)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF475569)
        )
    }
}
