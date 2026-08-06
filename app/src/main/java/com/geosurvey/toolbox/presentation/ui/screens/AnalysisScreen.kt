package com.geosurvey.toolbox.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.data.database.AttitudeEntity
import com.geosurvey.toolbox.presentation.ui.components.RoseDiagramView
import com.geosurvey.toolbox.presentation.ui.components.StereonetView
import com.geosurvey.toolbox.presentation.viewmodel.AnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF8FAFC))
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
                    text = "📊 地质分析",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
            Text(
                text = "${uiState.selectedAttitudes.size} 条数据",
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 统计信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F0FE).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("数据量", "${uiState.statistics.totalCount}")
                StatItem("平均走向", String.format("%.1f°", uiState.statistics.meanStrike))
                StatItem("平均倾角", String.format("%.1f°", uiState.statistics.meanDip))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 数据筛选
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.showAllData,
                onClick = { viewModel.toggleDataFilter(true) },
                label = { Text("全部数据") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !uiState.showAllData,
                onClick = { viewModel.toggleDataFilter(false) },
                label = { Text("最近10条") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab切换
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFFF1F5F9),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = Color(0xFF8B5CF6)
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("赤平投影", color = if (selectedTab == 0) Color(0xFF8B5CF6) else Color(0xFF475569)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("玫瑰花图", color = if (selectedTab == 1) Color(0xFF8B5CF6) else Color(0xFF475569)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("数据列表", color = if (selectedTab == 2) Color(0xFF8B5CF6) else Color(0xFF475569)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 内容区域
        when (selectedTab) {
            0 -> {
                // 赤平投影
                if (uiState.selectedAttitudes.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        StereonetView(
                            attitudes = uiState.selectedAttitudes,
                            modifier = Modifier.size(300.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔴 高倾角 >60°  🟡 中倾角 30-60°  🟢 低倾角 <30°",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据\n请先进行产状测量",
                            fontSize = 16.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
            1 -> {
                // 玫瑰花图
                if (uiState.selectedAttitudes.isNotEmpty()) {
                    val strikes = uiState.selectedAttitudes.map { it.strike }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        RoseDiagramView(
                            strikes = strikes,
                            modifier = Modifier.size(300.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📊 走向分布统计图",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据\n请先进行产状测量",
                            fontSize = 16.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
            2 -> {
                // 数据列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.selectedAttitudes) { attitude ->
                        AttitudeListItem(attitude = attitude)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
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

@Composable
fun AttitudeListItem(attitude: AttitudeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "走向: %.1f°".format(attitude.strike),
                fontSize = 13.sp,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "倾角: %.1f°".format(attitude.dip),
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )
            Text(
                text = "倾向: %.1f°".format(attitude.dipDirection),
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )
        }
    }
}
