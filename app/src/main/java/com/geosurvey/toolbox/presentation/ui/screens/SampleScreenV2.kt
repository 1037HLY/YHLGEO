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
import com.geosurvey.toolbox.data.database.DrillSampleEntity
import com.geosurvey.toolbox.data.database.SampleEntity
import com.geosurvey.toolbox.presentation.viewmodel.SampleViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScreenV2(
    viewModel: SampleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showFileNameDialog by remember { mutableStateOf(false) }
    var fileNameInput by remember { mutableStateOf("samples_${System.currentTimeMillis()}") }
    var exportType by remember { mutableStateOf("all") }

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
                IconButton(onClick = { 
                    showExportDialog = true 
                    exportType = "all"
                }) {
                    Icon(Icons.Default.Download, contentDescription = "导出", tint = Color(0xFF0EA5E9))
                }
                IconButton(onClick = { viewModel.loadAll() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Color(0xFF475569))
                }
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
                    IconButton(
                        onClick = { viewModel.startEditNormal(SampleEntity()) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加", tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                    }
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
                                onLongClick = { viewModel.startEditNormal(sample) },
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
                    IconButton(
                        onClick = { viewModel.startEditDrill(DrillSampleEntity()) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                    }
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
                                onLongClick = { viewModel.startEditDrill(sample) },
                                onDelete = { viewModel.deleteDrillSample(sample.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 导出对话框 - 使用独立函数
    if (showExportDialog) {
        ExportDialogSample(
            onDismiss = { showExportDialog = false },
            onExportNormal = {
                exportType = "normal"
                showExportDialog = false
                fileNameInput = "samples_${System.currentTimeMillis()}"
                showFileNameDialog = true
            },
            onExportDrill = {
                exportType = "drill"
                showExportDialog = false
                fileNameInput = "samples_${System.currentTimeMillis()}"
                showFileNameDialog = true
            },
            onExportAll = {
                exportType = "all"
                showExportDialog = false
                fileNameInput = "samples_${System.currentTimeMillis()}"
                showFileNameDialog = true
            }
        )
    }

    // 自定义文件名对话框
    if (showFileNameDialog) {
        FileNameDialogSample(
            fileName = fileNameInput,
            onFileNameChange = { fileNameInput = it },
            onConfirm = {
                val data = viewModel.exportSamples(exportType)
                try {
                    val file = File(context.filesDir, "$fileNameInput.csv")
                    file.writeText(data)
                    Toast.makeText(context, "导出成功: ${file.name}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                showFileNameDialog = false
            },
            onDismiss = { showFileNameDialog = false }
        )
    }

    // 普通样本编辑对话框 - 使用独立函数
    if (uiState.showNormalDialog) {
        NormalSampleDialogV2(
            sample = uiState.editingNormalSample ?: SampleEntity(),
            onSave = { sample ->
                if (sample.id == 0L) {
                    viewModel.addNormalSample(sample)
                } else {
                    viewModel.updateNormalSample(sample)
                }
                viewModel.closeDialogs()
            },
            onDismiss = { viewModel.closeDialogs() },
            currentLocation = uiState.currentLocation
        )
    }

    // 钻孔样本编辑对话框 - 使用独立函数
    if (uiState.showDrillDialog) {
        DrillSampleDialogV2(
            sample = uiState.editingDrillSample ?: DrillSampleEntity(),
            onSave = { sample ->
                if (sample.id == 0L) {
                    viewModel.addDrillSample(sample)
                } else {
                    viewModel.updateDrillSample(sample)
                }
                viewModel.closeDialogs()
            },
            onDismiss = { viewModel.closeDialogs() },
            currentLocation = uiState.currentLocation
        )
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

@Composable
fun ExportDialogSample(
    onDismiss: () -> Unit,
    onExportNormal: () -> Unit,
    onExportDrill: () -> Unit,
    onExportAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📤 导出样本数据") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("请选择数据类型：", fontSize = 14.sp, color = Color(0xFF475569))
                Button(
                    onClick = onExportNormal,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) { Text("导出普通样本") }
                Button(
                    onClick = onExportDrill,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) { Text("导出钻孔样本") }
                Button(
                    onClick = onExportAll,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) { Text("全选导出") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun FileNameDialogSample(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📝 自定义文件名") },
        text = {
            Column {
                Text("请输入文件名：", fontSize = 14.sp, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    label = { Text("文件名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text(".csv", fontSize = 14.sp, color = Color(0xFF94A3B8)) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("导出") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalSampleDialogV2(
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
                if (currentLocation != null) {
                    Text(
                        text = "📍 ${String.format("%.6f, %.6f", currentLocation.longitude, currentLocation.latitude)}",
                        fontSize = 11.sp, color = Color(0xFF94A3B8)
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
                onDismiss()
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
fun DrillSampleDialogV2(
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
                if (currentLocation != null) {
                    Text(
                        text = "📍 ${String.format("%.6f, %.6f", currentLocation.longitude, currentLocation.latitude)}",
                        fontSize = 11.sp, color = Color(0xFF94A3B8)
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
                onDismiss()
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
