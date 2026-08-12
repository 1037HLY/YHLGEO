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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geosurvey.toolbox.data.database.DrillSampleEntity
import com.geosurvey.toolbox.data.database.SampleEntity
import com.geosurvey.toolbox.presentation.theme.GlassColors
import com.geosurvey.toolbox.presentation.theme.GlassCardLight
import com.geosurvey.toolbox.presentation.utils.BackHandler
import com.geosurvey.toolbox.presentation.viewmodel.SampleViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScreenV3(
    viewModel: SampleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var expandedSection by remember { mutableStateOf<String?>(null) } // "normal" or "drill"
    var showExportDialog by remember { mutableStateOf(false) }
    var showNormalDialog by remember { mutableStateOf(false) }
    var showDrillDialog by remember { mutableStateOf(false) }
    var editingNormalSample by remember { mutableStateOf<SampleEntity?>(null) }
    var editingDrillSample by remember { mutableStateOf<DrillSampleEntity?>(null) }
    var fileNameInput by remember { mutableStateOf("samples_${System.currentTimeMillis()}") }
    var exportType by remember { mutableStateOf("all") }

    // 返回处理
    BackHandler(enabled = true) {
        if (expandedSection != null) {
            expandedSection = null
        } else {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { 
                    if (expandedSection != null) {
                        expandedSection = null
                    } else {
                        onBack()
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = GlassColors.TextPrimary)
                }
                Text(
                    "📋 样本管理",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassColors.TextPrimary
                )
            }
            IconButton(onClick = { showExportDialog = true }) {
                Icon(Icons.Default.Download, contentDescription = "导出", tint = GlassColors.PrimaryGlass)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 普通样本窗口
        val isNormalExpanded = expandedSection == "normal"
        GlassCardLight(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isNormalExpanded) 1f else 0.45f)
                .clickable { expandedSection = if (isNormalExpanded) null else "normal" }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📋 普通样本 (${uiState.normalSamples.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlassColors.SuccessGlass
                    )
                    Row {
                        if (isNormalExpanded) {
                            Text(
                                "点击收起",
                                fontSize = 12.sp,
                                color = GlassColors.TextTertiary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        IconButton(
                            onClick = { 
                                editingNormalSample = SampleEntity()
                                showNormalDialog = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加", tint = GlassColors.SuccessGlass, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (uiState.normalSamples.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无记录", fontSize = 13.sp, color = GlassColors.TextTertiary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val displayList = if (isNormalExpanded) uiState.normalSamples else uiState.normalSamples.take(5)
                        items(displayList) { sample ->
                            SampleItemGlass(
                                title = "${sample.sampleNumber} ${sample.name}",
                                subtitle = "类型: ${sample.sampleType} | ${sample.weight}kg",
                                onLongClick = {
                                    editingNormalSample = sample
                                    showNormalDialog = true
                                },
                                onDelete = { viewModel.deleteNormalSample(sample.id) }
                            )
                        }
                        if (!isNormalExpanded && uiState.normalSamples.size > 5) {
                            Text(
                                "... 还有 ${uiState.normalSamples.size - 5} 条，点击展开",
                                fontSize = 12.sp,
                                color = GlassColors.TextTertiary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 钻孔样本窗口
        val isDrillExpanded = expandedSection == "drill"
        GlassCardLight(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isDrillExpanded) 1f else 0.45f)
                .clickable { expandedSection = if (isDrillExpanded) null else "drill" }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔬 钻孔样本 (${uiState.drillSamples.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlassColors.PrimaryGlass
                    )
                    Row {
                        if (isDrillExpanded) {
                            Text(
                                "点击收起",
                                fontSize = 12.sp,
                                color = GlassColors.TextTertiary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                editingDrillSample = DrillSampleEntity()
                                showDrillDialog = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加", tint = GlassColors.PrimaryGlass, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (uiState.drillSamples.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无记录", fontSize = 13.sp, color = GlassColors.TextTertiary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val displayList = if (isDrillExpanded) uiState.drillSamples else uiState.drillSamples.take(5)
                        items(displayList) { sample ->
                            SampleItemGlass(
                                title = "${sample.sampleNumber} ${sample.name}",
                                subtitle = "孔深: ${sample.depthFrom}-${sample.depthTo}m | 样长: ${sample.sampleLength}m",
                                onLongClick = {
                                    editingDrillSample = sample
                                    showDrillDialog = true
                                },
                                onDelete = { viewModel.deleteDrillSample(sample.id) }
                            )
                        }
                        if (!isDrillExpanded && uiState.drillSamples.size > 5) {
                            Text(
                                "... 还有 ${uiState.drillSamples.size - 5} 条，点击展开",
                                fontSize = 12.sp,
                                color = GlassColors.TextTertiary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 导出对话框
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("📤 导出样本数据", color = GlassColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("请选择数据类型：", fontSize = 14.sp, color = GlassColors.TextSecondary)
                    Button(
                        onClick = {
                            exportType = "normal"
                            showExportDialog = false
                            fileNameInput = "samples_${System.currentTimeMillis()}"
                            val data = viewModel.exportSamples("normal")
                            try {
                                val file = File(context.filesDir, "$fileNameInput.csv")
                                file.writeText(data)
                                Toast.makeText(context, "导出成功: ${file.name}", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassColors.SuccessGlass)
                    ) { Text("导出普通样本", color = Color.White) }
                    Button(
                        onClick = {
                            exportType = "drill"
                            showExportDialog = false
                            fileNameInput = "samples_${System.currentTimeMillis()}"
                            val data = viewModel.exportSamples("drill")
                            try {
                                val file = File(context.filesDir, "$fileNameInput.csv")
                                file.writeText(data)
                                Toast.makeText(context, "导出成功: ${file.name}", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassColors.PrimaryGlass)
                    ) { Text("导出钻孔样本", color = Color.White) }
                    Button(
                        onClick = {
                            exportType = "all"
                            showExportDialog = false
                            fileNameInput = "samples_${System.currentTimeMillis()}"
                            val data = viewModel.exportSamples("all")
                            try {
                                val file = File(context.filesDir, "$fileNameInput.csv")
                                file.writeText(data)
                                Toast.makeText(context, "导出成功: ${file.name}", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassColors.SecondaryGlass)
                    ) { Text("全选导出", color = Color.White) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("取消", color = GlassColors.TextSecondary) }
            }
        )
    }

    // 普通样本编辑对话框
    if (showNormalDialog && editingNormalSample != null) {
        val sample = editingNormalSample!!
        var sampleType by remember { mutableStateOf(sample.sampleType) }
        var sampleNumber by remember { mutableStateOf(sample.sampleNumber) }
        var name by remember { mutableStateOf(sample.name) }
        var weight by remember { mutableStateOf(sample.weight) }
        var description by remember { mutableStateOf(sample.description) }

        AlertDialog(
            onDismissRequest = {
                showNormalDialog = false
                editingNormalSample = null
            },
            title = { Text(if (sample.id == 0L) "添加普通样本" else "编辑普通样本", color = GlassColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = sampleType, onValueChange = { sampleType = it },
                        label = { Text("样本类型") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = sampleNumber, onValueChange = { sampleNumber = it },
                        label = { Text("编号") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("名称") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = weight, onValueChange = { weight = it },
                        label = { Text("重量 (kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), maxLines = 2
                    )
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
                        latitude = uiState.currentLocation?.latitude ?: sample.latitude,
                        longitude = uiState.currentLocation?.longitude ?: sample.longitude,
                        altitude = uiState.currentLocation?.altitude ?: sample.altitude,
                        timestamp = if (sample.id == 0L) System.currentTimeMillis() else sample.timestamp
                    )
                    if (sample.id == 0L) {
                        viewModel.addNormalSample(newSample)
                    } else {
                        viewModel.updateNormalSample(newSample)
                    }
                    showNormalDialog = false
                    editingNormalSample = null
                }) {
                    Text("保存", color = GlassColors.SuccessGlass)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNormalDialog = false
                    editingNormalSample = null
                }) { Text("取消", color = GlassColors.TextSecondary) }
            }
        )
    }

    // 钻孔样本编辑对话框
    if (showDrillDialog && editingDrillSample != null) {
        val sample = editingDrillSample!!
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
            onDismissRequest = {
                showDrillDialog = false
                editingDrillSample = null
            },
            title = { Text(if (sample.id == 0L) "添加钻孔样本" else "编辑钻孔样本", color = GlassColors.TextPrimary) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    OutlinedTextField(
                        value = sampleNumber, onValueChange = { sampleNumber = it },
                        label = { Text("样本编号") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = depthFrom, onValueChange = { depthFrom = it },
                            label = { Text("孔深自") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                        OutlinedTextField(
                            value = depthTo, onValueChange = { depthTo = it },
                            label = { Text("孔深至") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = sampleLength, onValueChange = { sampleLength = it },
                            label = { Text("样长") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                        OutlinedTextField(
                            value = coreLength, onValueChange = { coreLength = it },
                            label = { Text("岩心长") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = recoveryRate, onValueChange = { recoveryRate = it },
                            label = { Text("采取率") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                        OutlinedTextField(
                            value = weight, onValueChange = { weight = it },
                            label = { Text("重量") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("名称") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                        OutlinedTextField(
                            value = coreDiameter, onValueChange = { coreDiameter = it },
                            label = { Text("岩心直径") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), maxLines = 2
                    )
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
                        latitude = uiState.currentLocation?.latitude ?: sample.latitude,
                        longitude = uiState.currentLocation?.longitude ?: sample.longitude,
                        altitude = uiState.currentLocation?.altitude ?: sample.altitude,
                        timestamp = if (sample.id == 0L) System.currentTimeMillis() else sample.timestamp
                    )
                    if (sample.id == 0L) {
                        viewModel.addDrillSample(newSample)
                    } else {
                        viewModel.updateDrillSample(newSample)
                    }
                    showDrillDialog = false
                    editingDrillSample = null
                }) {
                    Text("保存", color = GlassColors.PrimaryGlass)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDrillDialog = false
                    editingDrillSample = null
                }) { Text("取消", color = GlassColors.TextSecondary) }
            }
        )
    }
}

@Composable
fun SampleItemGlass(
    title: String,
    subtitle: String,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onLongClick() }
            .background(GlassColors.GlassBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GlassColors.TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = GlassColors.TextSecondary)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "删除", tint = GlassColors.ErrorGlass, modifier = Modifier.size(14.dp))
        }
    }
}
