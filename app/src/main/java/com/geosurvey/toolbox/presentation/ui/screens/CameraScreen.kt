package com.geosurvey.toolbox.presentation.ui.screens

import android.Manifest
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.geosurvey.toolbox.data.database.PhotoEntity
import com.geosurvey.toolbox.presentation.viewmodel.CameraViewModel
import com.geosurvey.toolbox.presentation.viewmodel.WatermarkConfig
import com.geosurvey.toolbox.presentation.viewmodel.WatermarkPosition
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWatermarkSettings by remember { mutableStateOf(false) }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // 相机启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            previewPhotoPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                if (bitmap != null) {
                    viewModel.savePhotoWithWatermark(bitmap)
                }
            }
            previewPhotoPath = null
        }
    }

    // 权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            // 提示用户权限被拒绝
        }
    }

    // 检查并请求权限
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

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
            Text(
                text = "📷 水印相机",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Row {
                IconButton(onClick = { showWatermarkSettings = !showWatermarkSettings }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "水印设置",
                        tint = Color(0xFF0EA5E9)
                    )
                }
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "返回",
                        tint = Color(0xFF475569)
                    )
                }
            }
        }

        // 拍照按钮和预览区
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F0FE).copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.lastPhotoPath != null) {
                    val bitmap = runCatching {
                        BitmapFactory.decodeFile(uiState.lastPhotoPath)
                    }.getOrNull()
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "最近照片",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text(
                        text = "📷 点击下方按钮拍照",
                        fontSize = 18.sp,
                        color = Color(0xFF475569)
                    )
                }

                // 拍摄按钮
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .size(72.dp)
                        .background(Color(0xFF0EA5E9), RoundedCornerShape(36.dp))
                        .clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                val photoFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                                previewPhotoPath = photoFile.absolutePath
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White, RoundedCornerShape(30.dp))
                    )
                }
            }
        }

        // 产状输入区
        if (uiState.watermarkConfig.showAttitude) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F3FF).copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📐 产状信息",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8B5CF6)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.strike?.toString() ?: "",
                            onValueChange = {
                                val value = it.toFloatOrNull()
                                viewModel.updateAttitude(value, uiState.dip, uiState.dipDirection)
                            },
                            label = { Text("走向 (°)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.dip?.toString() ?: "",
                            onValueChange = {
                                val value = it.toFloatOrNull()
                                viewModel.updateAttitude(uiState.strike, value, uiState.dipDirection)
                            },
                            label = { Text("倾角 (°)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.dipDirection?.toString() ?: "",
                            onValueChange = {
                                val value = it.toFloatOrNull()
                                viewModel.updateAttitude(uiState.strike, uiState.dip, value)
                            },
                            label = { Text("倾向 (°)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // 备注输入
        if (uiState.watermarkConfig.showNote) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text("📝 备注") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 2
            )
        }

        // 水印设置弹窗
        if (showWatermarkSettings) {
            WatermarkSettingsDialog(
                config = uiState.watermarkConfig,
                onConfigChange = { viewModel.updateWatermarkConfig(it) },
                onDismiss = { showWatermarkSettings = false }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 历史照片列表
        Text(
            text = "📋 历史照片 (${uiState.photoList.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.photoList) { photo ->
                PhotoHistoryItem(
                    photo = photo,
                    onView = {
                        // 查看照片
                    },
                    onDelete = { viewModel.deletePhoto(photo.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkSettingsDialog(
    config: WatermarkConfig,
    onConfigChange: (WatermarkConfig) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚙️ 水印设置") },
        text = {
            Column {
                // 内容选项
                Text("显示内容", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row {
                    Checkbox(
                        checked = config.showCoordinates,
                        onCheckedChange = { onConfigChange(config.copy(showCoordinates = it)) }
                    )
                    Text("坐标")
                }
                Row {
                    Checkbox(
                        checked = config.showTime,
                        onCheckedChange = { onConfigChange(config.copy(showTime = it)) }
                    )
                    Text("时间")
                }
                Row {
                    Checkbox(
                        checked = config.showLocation,
                        onCheckedChange = { onConfigChange(config.copy(showLocation = it)) }
                    )
                    Text("地点")
                }
                Row {
                    Checkbox(
                        checked = config.showAttitude,
                        onCheckedChange = { onConfigChange(config.copy(showAttitude = it)) }
                    )
                    Text("产状")
                }
                Row {
                    Checkbox(
                        checked = config.showNote,
                        onCheckedChange = { onConfigChange(config.copy(showNote = it)) }
                    )
                    Text("备注")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 字体大小
                Text("字体大小: ${config.fontSize.toInt()}px", fontSize = 14.sp)
                Slider(
                    value = config.fontSize,
                    onValueChange = { onConfigChange(config.copy(fontSize = it)) },
                    valueRange = 20f..80f,
                    steps = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 位置
                Text("位置", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row {
                    WatermarkPosition.values().forEach { pos ->
                        FilterChip(
                            selected = config.position == pos,
                            onClick = { onConfigChange(config.copy(position = pos)) },
                            label = { Text(pos.name.replace("_", " ")) },
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 透明度
                Text("透明度: ${(config.opacity * 100).toInt()}%", fontSize = 14.sp)
                Slider(
                    value = config.opacity,
                    onValueChange = { onConfigChange(config.copy(opacity = it)) },
                    valueRange = 0.1f..1.0f,
                    steps = 9
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoHistoryItem(
    photo: PhotoEntity,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = "📍 %.6f, %.6f".format(photo.latitude, photo.longitude),
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "🕐 ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(photo.timestamp))}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                if (photo.strike != null && photo.dip != null) {
                    Text(
                        text = "📐 走向: %.1f° 倾角: %.1f°".format(photo.strike!!, photo.dip!!),
                        fontSize = 12.sp,
                        color = Color(0xFF8B5CF6)
                    )
                }
                if (!photo.note.isNullOrEmpty()) {
                    Text(
                        text = "📝 ${photo.note}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
            Row {
                IconButton(onClick = onView) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "查看",
                        tint = Color(0xFF0EA5E9)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
