package com.geosurvey.toolbox.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object GlassColors {
    // 液态玻璃主色调
    val GlassBackground = Color(0xCCFFFFFF)
    val GlassBackgroundLight = Color(0x66FFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val GlassShadow = Color(0x1A000000)
    val GlassShadowLight = Color(0x0D000000)
    
    // 功能色
    val Primary = Color(0xFF0EA5E9)
    val PrimaryLight = Color(0xFF7DD3FC)
    val PrimaryDark = Color(0xFF0284C7)
    val Secondary = Color(0xFF8B5CF6)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    
    // 文字色
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF4A4A5A)
    val TextTertiary = Color(0xFF8A8A9A)
    val TextOnGlass = Color(0xFFFFFFFF)
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape = shape,
            clip = false,
            ambientColor = GlassColors.GlassShadow,
            spotColor = GlassColors.GlassShadow
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = GlassColors.GlassBackground
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        content()
    }
}

@Composable
fun GlassCardLight(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = shape,
            clip = false,
            ambientColor = GlassColors.GlassShadow,
            spotColor = GlassColors.GlassShadow
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = GlassColors.GlassBackgroundLight
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        content()
    }
}
