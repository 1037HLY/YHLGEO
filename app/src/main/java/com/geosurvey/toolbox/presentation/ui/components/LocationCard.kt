package com.geosurvey.toolbox.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.domain.model.LocationPoint
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LocationCard(
    location: LocationPoint?,
    satelliteCount: Int,
    qualityText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 GPS定位",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0EA5E9)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = qualityText,
                    fontSize = 14.sp,
                    color = if (qualityText.contains("优秀")) Color(0xFF10B981) else Color(0xFF475569)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (location != null) {
                // 经纬度
                Text(
                    text = "经度: %.6f  纬度: %.6f".format(location.longitude, location.latitude),
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 海拔和速度
                Text(
                    text = "海拔: %.1fm  速度: %.1fkm/h".format(location.altitude, location.speed * 3.6),
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 卫星信息
                Text(
                    text = "🛰️ 卫星: $satelliteCount 颗  HDOP: %.1f".format(location.gnssInfo?.hdop ?: 0f),
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
                // 时间
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                Text(
                    text = "🕐 ${timeFormat.format(location.timestamp)}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            } else {
                Text(
                    text = "正在获取定位... 请确保GPS已开启",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}
