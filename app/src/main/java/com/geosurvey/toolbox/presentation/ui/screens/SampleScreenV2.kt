package com.geosurvey.toolbox.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.data.database.DrillSampleEntity
import com.geosurvey.toolbox.data.database.SampleEntity
import com.geosurvey.toolbox.presentation.viewmodel.SampleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScreenV2(
    viewModel: SampleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                Text("📋 样本记录", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
            IconButton(onClick = { viewModel.loadAll() }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Color(0xFF475569))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 普通样本区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 0.dp, max = 250.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0FDF4).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 普通样本 (${uiState.normalSamples.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = "点击条目编辑",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (uiState.normalSamples.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无记录", fontSize = 13.sp, color = Color(0xFF94A3B8))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.normalSamples.take(5)) { sample ->
                            SampleItemSmall(
                                title = "${sample.sampleNumber} ${sample.name}",
                                subtitle = "类型: ${sample.sampleType} | ${sample.weight}kg",
                                onLongClick = { 
                                    Toast.makeText(context, "编辑: ${sample.name}", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { viewModel.deleteNormalSample(sample.id) }
                            )
                        }
                        if (uiState.normalSamples.size > 5) {
                            Text(
                                text = "... 还有 ${uiState.normalSamples.size - 5} 条",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 钻孔样本区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEFF6FF).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔬 钻孔样本 (${uiState.drillSamples.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                    Text(
                        text = "点击条目编辑",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (uiState.drillSamples.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无记录", fontSize = 13.sp, color = Color(0xFF94A3B8))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.drillSamples) { sample ->
                            SampleItemSmall(
                                title = "${sample.sampleNumber} ${sample.name}",
                                subtitle = "孔深: ${sample.depthFrom}-${sample.depthTo}m | 样长: ${sample.sampleLength}m",
                                onLongClick = { 
                                    Toast.makeText(context, "编辑: ${sample.name}", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { viewModel.deleteDrillSample(sample.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SampleItemSmall(
    title: String,
    subtitle: String,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLongClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF475569))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
        }
    }
}
