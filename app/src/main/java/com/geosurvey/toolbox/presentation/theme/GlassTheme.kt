package com.geosurvey.toolbox.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object GlassColors {
    val GlassBackground = Color(0xCCFFFFFF)  // 半透明白
    val GlassBorder = Color(0x33FFFFFF)      // 半透明白边框
    val GlassShadow = Color(0x1A000000)      // 半透黑阴影
    val PrimaryGlass = Color(0x0EA5E9)
    val SecondaryGlass = Color(0x8B5CF6)
    val SuccessGlass = Color(0x10B981)
    val WarningGlass = Color(0xF59E0B)
}

@Composable
fun GlassCard(
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xCCFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        content()
    }
}
