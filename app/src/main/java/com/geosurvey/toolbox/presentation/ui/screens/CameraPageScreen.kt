package com.geosurvey.toolbox.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPageScreen(
    onBack: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color(0xFF475569))
                }
                Text("📷 水印相机", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 占位内容
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("📷", fontSize = 80.sp)
                Text(
                    text = "水印相机功能开发中",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "拍照并叠加坐标/时间/产状水印\n将在后续版本中实现",
                    fontSize = 16.sp,
                    color = Color(0xFF94A3B8)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0).copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋 计划功能", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("• 拍照 + 水印叠加", fontSize = 13.sp, color = Color(0xFF475569))
                        Text("• 坐标/时间/产状/备注", fontSize = 13.sp, color = Color(0xFF475569))
                        Text("• 水印位置/大小/透明度调整", fontSize = 13.sp, color = Color(0xFF475569))
                        Text("• 保存到相册 + 历史记录", fontSize = 13.sp, color = Color(0xFF475569))
                    }
                }
            }
        }
    }
}
