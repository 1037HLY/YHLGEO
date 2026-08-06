package com.geosurvey.toolbox.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.presentation.viewmodel.TrackSummary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackListScreen(
    tracks: List<TrackSummary>,
    onTrackClick: (String) -> Unit,
    onDeleteTrack: (String) -> Unit,
    onBack: () -> Unit
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
                text = "📋 历史轨迹",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Button(onClick = onBack) {
                Text("返回")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无轨迹记录\n\n开始一次轨迹记录吧！",
                    fontSize = 16.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            LazyColumn {
                items(tracks) { track ->
                    TrackItem(
                        track = track,
                        onItemClick = { onTrackClick(track.trackId) },
                        onDelete = { onDeleteTrack(track.trackId) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TrackItem(
    track: TrackSummary,
    onItemClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🕐 ${formatTime(track.startTime)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "📍 ${track.pointCount} 个点",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                if (track.endTime != null) {
                    Text(
                        text = "结束: ${formatTime(track.endTime)}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row {
                TextButton(onClick = onItemClick) {
                    Text("查看", fontSize = 12.sp)
                }
                TextButton(onClick = onDelete) {
                    Text("删除", fontSize = 12.sp, color = Color(0xFFEF4444))
                }
            }
        }
    }
}

// 使用 Long 时间戳格式化
fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}
