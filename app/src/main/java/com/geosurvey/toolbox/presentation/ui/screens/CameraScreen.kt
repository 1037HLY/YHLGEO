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
    var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val context = LocalContext.current

    // 相机启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            previewPhotoPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                if (bitmap != null) {
                    capturedBitmap = bitmap
                    // 保存带水印的照片
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

    // 自动加载照片列表
    LaunchedEffect(Unit) {
        viewModel.loadPhotos()
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

        Spacer(modifier = Modifier.height(8.dp))

        // 相机预览窗口（实时预览）
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 显示最新拍摄的照片
                val displayBitmap = if (uiState.lastPhotoPath != null) {
                    runCatching {
                        BitmapFactory.decodeFile(uiState.lastPhotoPath)
                    }.getOrNull()
                } else {
                    capturedBitmap
                }

                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = "拍摄的照片",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 预览占位
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击下方按钮拍照",
                            fontSize = 16.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "照片将自动添加水印并保存到相册",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // 拍摄按钮（覆盖在预览窗口底部）
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

        Spacer(modifier = Modifier.height(8.dp))

        // 产状输入区
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F3FF).copy(alpha = 0.8f)
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
                    color = Color(0xFF5B21B6)
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
                        label = { Text("走向 (°)", color = Color(0xFF374151)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1F2937),
                            unfocusedTextColor = Color(0xFF1F2937)
                        )
                    )
                    OutlinedTextField(
                        value = uiState.dip?.toString() ?: "",
                        onValueChange = {
                            val value = it.toFloatOrNull()
                            viewModel.updateAttitude(uiState.strike, value, uiState.dipDirection)
                        },
                        label = { Text("倾角 (°)", color = Color(0xFF374151)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1F2937),
                            unfocusedTextColor = Color(0xFF1F2937)
                        )
                    )
                    OutlinedTextField(
                        value = uiState.dipDirection?.toString() ?: "",
                        onValueChange = {
                            val value = it.toFloatOrNull()
                            viewModel.updateAttitude(uiState.strike, uiState.dip, value)
                        },
                        label = { Text("倾向 (°)", color = Color(0xFF374151)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1F2937),
                            unfocusedTextColor = Color(0xFF1F2937)
                        )
                    )
                }
            }
        }

        // 备注输入
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.updateNote(it) },
            label = { Text("📝 备注", color = Color(0xFF374151)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1F2937),
                unfocusedTextColor = Color(0xFF1F2937)
            )
        )

        // 水印设置弹窗
        if (showWatermarkSettings) {
            WatermarkSettingsDialog(
                config = uiState.watermarkConfig,
                onConfigChange = { viewModel.updateWatermarkConfig(it) },
                onDismiss = { showWatermarkSettings = false }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 历史照片列表
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 历史照片 (${uiState.photoList.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            TextButton(
                onClick = { viewModel.loadPhotos() }
            ) {
                Text("刷新", fontSize = 12.sp, color = Color(0xFF0EA5E9))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.photoList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无照片，点击上方按钮拍摄",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
        title = { Text("⚙️ 水印设置", color = Color(0xFF0F172A)) },
        text = {
            Column {
                Text("显示内容", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = config.showCoordinates,
                        onCheckedChange = { onConfigChange(config.copy(showCoordinates = it)) }
                    )
                    Text("坐标", color = Color(0xFF1F2937))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = config.showTime,
                        onCheckedChange = { onConfigChange(config.copy(showTime = it)) }
                    )
                    Text("时间", color = Color(0xFF1F2937))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = config.showAttitude,
                        onCheckedChange = { onConfigChange(config.copy(showAttitude = it)) }
                    )
                    Text("产状", color = Color(0xFF1F2937))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = config.showNote,
                        onCheckedChange = { onConfigChange(config.copy(showNote = it)) }
                    )
                    Text("备注", color = Color(0xFF1F2937))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("字体大小: ${config.fontSize.toInt()}px", fontSize = 14.sp, color = Color(0xFF0F172A))
                Slider(
                    value = config.fontSize,
                    onValueChange = { onConfigChange(config.copy(fontSize = it)) },
                    valueRange = 20f..80f,
                    steps = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("位置", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Row {
                    WatermarkPosition.values().forEach { pos ->
                        FilterChip(
                            selected = config.position == pos,
                            onClick = { onConfigChange(config.copy(position = pos)) },
                            label = { Text(pos.name.replace("_", " "), fontSize = 10.sp) },
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("透明度: ${(config.opacity * 100).toInt()}%", fontSize = 14.sp, color = Color(0xFF0F172A))
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
                Text("完成", color = Color(0xFF0EA5E9))
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
            containerColor = Color(0xFFF8FAFC).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 缩略图
            val thumbnail = runCatching {
                BitmapFactory.decodeFile(photo.imagePath)?.let {
                    android.graphics.Bitmap.createScaledBitmap(it, 80, 80, true)
                }
            }.getOrNull()

            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = "缩略图",
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 照片信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "📍 %.6f, %.6f".format(photo.latitude, photo.longitude),
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "🕐 ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(photo.timestamp))}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
                if (photo.strike != null && photo.dip != null) {
                    Text(
                        text = "📐 走向: %.1f° 倾角: %.1f°".format(photo.strike!!, photo.dip!!),
                        fontSize = 11.sp,
                        color = Color(0xFF7C3AED)
                    )
                }
                if (!photo.note.isNullOrEmpty()) {
                    Text(
                        text = "📝 ${photo.note}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        maxLines = 1
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onView,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "查看",
                        tint = Color(0xFF0EA5E9),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
