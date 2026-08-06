@Composable
fun TrackItem(
    track: TrackSummary,
    onItemClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
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
                    text = "🕐 ${formatTime(track.startTime)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "📍 ${track.pointCount} 个点",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                if (track.endTime != null) {
                    Text(
                        text = "结束: ${formatTime(track.endTime)}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row {
                TextButton(onClick = onItemClick) {
                    Text("查看", fontSize = 12.sp)
                }
                TextButton(onClick = onDelete) {
                    Text("删除", fontSize = 12.sp, color = Color(0xFFEF4444))
                }
            }
        }
    }
}

// 使用 Long 时间戳格式化
private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}
