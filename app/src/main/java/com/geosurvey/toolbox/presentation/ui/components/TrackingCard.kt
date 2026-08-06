package com.geosurvey.toolbox.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrackingCard(
    isRecording: Boolean,
    pointCount: Int,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onViewTracksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) {
                Color(0xFFECFDF5).copy(alpha = 0.8f)
            } else {
                Color(0xFFECFDF5).copy(alpha = 0.6f)
            }
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛣️ 轨迹记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981)
                )
                if (isRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "● 记录中",
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$pointCount 点",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                } else {
                    Text(
                        text = "已停止",
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRecording) {
                    Button(
                        onClick = onStopClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("停止记录")
                    }
                } else {
                    Button(
                        onClick = onStartClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("开始记录")
                    }
                }

                Button(
                    onClick = onViewTracksClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0EA5E9)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("历史轨迹")
                }
            }

            if (isRecording) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 后台持续记录中，熄屏不中断",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
