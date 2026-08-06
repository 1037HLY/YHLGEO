package com.geosurvey.toolbox.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.presentation.SatelliteDetail

@Composable
fun SatelliteDetailList(
    satellites: List<SatelliteDetail>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFFF1F5F9).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        // 表头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("编号", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
            Text("星座", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
            Text("信噪比", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
            Text("仰角", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
            Text("状态", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
        }

        LazyColumn {
            items(satellites) { sat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sat.prn.toString(), fontSize = 13.sp, modifier = Modifier.weight(1f), color = Color(0xFF0F172A))
                    Text(
                        sat.constellation,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        color = when (sat.constellation) {
                            "GPS" -> Color(0xFF0EA5E9)
                            "GLONASS" -> Color(0xFF10B981)
                            "Galileo" -> Color(0xFF8B5CF6)
                            "北斗" -> Color(0xFFEF4444)
                            else -> Color(0xFF475569)
                        }
                    )
                    Text(
                        String.format("%.1f", sat.snr),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        color = when {
                            sat.snr > 30 -> Color(0xFF10B981)
                            sat.snr > 20 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }
                    )
                    Text(String.format("%.0f°", sat.elevation), fontSize = 13.sp, modifier = Modifier.weight(1f), color = Color(0xFF0F172A))
                    Text(
                        if (sat.usedInFix) "✅" else "⏳",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = if (sat.usedInFix) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
