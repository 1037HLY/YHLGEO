package com.geosurvey.toolbox.presentation.ui.screens

import android.content.Context
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
import androidx.compose.ui.window.Dialog
import com.geosurvey.toolbox.data.database.DrillSampleEntity
import com.geosurvey.toolbox.data.database.SampleEntity
import com.geosurvey.toolbox.presentation.viewmodel.SampleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScreen(
    viewModel: SampleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }

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
            Row {
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(Icons.Default.Download, contentDescription = "导出", tint = Color(0xFF0EA5E9))
                }
                IconButton(onClick = { viewModel.loadAll() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Color(0xFF475569))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 上下两个窗口使用 TabRow 切换
        var selectedTab by remember { mutableStateOf(0) }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFFF1F5F9),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = Color(0xFF0EA5E9)
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("普通样本", color = if (selectedTab == 0) Color(0xFF0EA5E9) else Color(0xFF475569)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("钻孔样本", color = if (selectedTab == 1) Color(0xFF0EA5E9) else Color(0xFF475569)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> NormalSampleList(viewModel, uiState.normalSamples)
            1 -> DrillSampleList(viewModel, uiState.drillSamples)
        }

        // 添加按钮
        FloatingActionButton(
            onClick = {
                if (selectedTab == 0) {
                    viewModel.startEditNormal(SampleEntity())
                } else {
                    viewModel.startEditDrill(DrillSampleEntity())
                }
            },
            containerColor = Color(0xFF0EA5E9),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加", tint = Color.White)
        }
    }

    // 导出对话框
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { type ->
                val data = viewModel.exportSamples(type)
                // 保存到文件
                val fileName = "samples_${System.currentTimeMillis()}.csv"
                try {
                    context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                        it.write(data.toByteArray())
                    }
                    Toast.makeText(context, "导出成功: $fileName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                showExportDialog = false
            }
        )
    }

    // 普通样本编辑对话框
    if (uiState.showNormalDialog) {
        NormalSampleDialog(
            sample = uiState.editingNormalSample ?: SampleEntity(),
            onSave = { sample ->
                if (sample.id == 0L) {
                    viewModel.addNormalSample(sample)
                } else {
                    viewModel.updateNormalSample(sample)
                }
            },
            onDismiss = { viewModel.closeDialogs() },
            currentLocation = uiState.currentLocation
        )
    }

    // 钻孔样本编辑对话框
    if (uiState.showDrillDialog) {
        DrillSampleDialog(
            sample = uiState.editingDrillSample ?: DrillSampleEntity(),
            onSave = { sample ->
                if (sample.id == 0L) {
                    viewModel.addDrillSample(sample)
                } else {
                    viewModel.updateDrillSample(sample)
                }
            },
            onDismiss = { viewModel.closeDialogs() },
            currentLocation = uiState.currentLocation
        )
    }
}

@Composable
fun NormalSampleList(viewModel: SampleViewModel, samples: List<SampleEntity>) {
    if (samples.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无普通样本记录\n点击右下角 + 添加", fontSize = 16.sp, color = Color(0xFF94A3B8))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(samples) { sample ->
                SampleItem(
                    title = "${sample.sampleNumber} ${sample.name}",
                    subtitle = "类型: ${sample.sampleType} | 重量: ${sample.weight}",
                    description = sample.description,
                    onLongClick = { viewModel.startEditNormal(sample) },
                    onDelete = { viewModel.deleteNormalSample(sample.id) }
                )
            }
        }
    }
}

@Composable
fun DrillSampleList(viewModel: SampleViewModel, samples: List<DrillSampleEntity>) {
    if (samples.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无钻孔样本记录\n点击右下角 + 添加", fontSize = 16.sp, color = Color(0xFF94A3B8))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(samples) { sample ->
                SampleItem(
                    title = "${sample.sampleNumber} ${sample.name}",
                    subtitle = "孔深: ${sample.depthFrom}-${sample.depthTo}m | 样长: ${sample.sampleLength}m",
                    description = "采取率: ${sample.recoveryRate}% | ${sample.description}",
                    onLongClick = { viewModel.startEditDrill(sample) },
                    onDelete = { viewModel.deleteDrillSample(sample.id) }
                )
            }
        }
    }
}

@Composable
fun SampleItem(
    title: String,
    subtitle: String,
    description: String,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 点击查看详情 */ }
            .clickable { onLongClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC).copy(alpha = 0.9f)
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
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF475569))
                if (description.isNotEmpty()) {
                    Text(description, fontSize = 12.sp, color = Color(0xFF94A3B8), maxLines = 1)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalSampleDialog(
    sample: SampleEntity,
    onSave: (SampleEntity) -> Unit,
    onDismiss: () -> Unit,
    currentLocation: android.location.Location?
) {
    var sampleType by remember { mutableStateOf(sample.sampleType) }
    var sampleNumber by remember { mutableStateOf(sample.sampleNumber) }
    var name by remember { mutableStateOf(sample.name) }
    var weight by remember { mutableStateOf(sample.weight) }
    var description by remember { mutableStateOf(sample.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (sample.id == 0L) "添加普通样本" else "编辑普通样本") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = sampleType,
                    onValueChange = { sampleType = it },
                    label = { Text("样本类型") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = sampleNumber,
                    onValueChange = { sampleNumber = it },
                    label = { Text("编号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("重量 (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                if (currentLocation != null) {
                    Text(
                        text = "📍 ${String.format("%.6f, %.6f", currentLocation.longitude, currentLocation.latitude)}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newSample = sample.copy(
                    sampleType = sampleType,
                    sampleNumber = sampleNumber,
                    name = name,
                    weight = weight,
                    description = description,
                    latitude = currentLocation?.latitude ?: sample.latitude,
                    longitude = currentLocation?.longitude ?: sample.longitude,
                    altitude = currentLocation?.altitude ?: sample.altitude,
                    timestamp = if (sample.id == 0L) System.currentTimeMillis() else sample.timestamp
                )
                onSave(newSample)
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillSampleDialog(
    sample: DrillSampleEntity,
    onSave: (DrillSampleEntity) -> Unit,
    onDismiss: () -> Unit,
    currentLocation: android.location.Location?
) {
    var sampleNumber by remember { mutableStateOf(sample.sampleNumber) }
    var depthFrom by remember { mutableStateOf(sample.depthFrom) }
    var depthTo by remember { mutableStateOf(sample.depthTo) }
    var sampleLength by remember { mutableStateOf(sample.sampleLength) }
    var coreLength by remember { mutableStateOf(sample.coreLength) }
    var recoveryRate by remember { mutableStateOf(sample.recoveryRate) }
    var weight by remember { mutableStateOf(sample.weight) }
    var name by remember { mutableStateOf(sample.name) }
    var coreDiameter by remember { mutableStateOf(sample.coreDiameter) }
    var description by remember { mutableStateOf(sample.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (sample.id == 0L) "添加钻孔样本" else "编辑钻孔样本") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                OutlinedTextField(
                    value = sampleNumber,
                    onValueChange = { sampleNumber = it },
                    label = { Text("样本编号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = depthFrom,
                        onValueChange = { depthFrom = it },
                        label = { Text("孔深自 (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = depthTo,
                        onValueChange = { depthTo = it },
                        label = { Text("孔深至 (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sampleLength,
                        onValueChange = { sampleLength = it },
                        label = { Text("样长 (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = coreLength,
                        onValueChange = { coreLength = it },
                        label = { Text("岩心长 (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = recoveryRate,
                        onValueChange = { recoveryRate = it },
                        label = { Text("采取率 (%)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("重量 (kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("名称") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = coreDiameter,
                        onValueChange = { coreDiameter = it },
                        label = { Text("岩心直径 (mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                if (currentLocation != null) {
                    Text(
                        text = "📍 ${String.format("%.6f, %.6f", currentLocation.longitude, currentLocation.latitude)}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newSample = sample.copy(
                    sampleNumber = sampleNumber,
                    depthFrom = depthFrom,
                    depthTo = depthTo,
                    sampleLength = sampleLength,
                    coreLength = coreLength,
                    recoveryRate = recoveryRate,
                    weight = weight,
                    name = name,
                    coreDiameter = coreDiameter,
                    description = description,
                    latitude = currentLocation?.latitude ?: sample.latitude,
                    longitude = currentLocation?.longitude ?: sample.longitude,
                    altitude = currentLocation?.altitude ?: sample.altitude,
                    timestamp = if (sample.id == 0L) System.currentTimeMillis() else sample.timestamp
                )
                onSave(newSample)
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📤 导出样本数据") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("请选择要导出的数据类型：", fontSize = 14.sp, color = Color(0xFF475569))
                Button(
                    onClick = { onExport("normal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Text("导出普通样本")
                }
                Button(
                    onClick = { onExport("drill") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("导出钻孔样本")
                }
                Button(
                    onClick = { onExport("all") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("全选导出")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
