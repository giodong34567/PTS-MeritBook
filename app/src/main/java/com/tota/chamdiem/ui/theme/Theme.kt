package com.tota.chamdiem.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E6F5C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA6F2DD),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4A635C),
    tertiary = Color(0xFF416277),
    background = Color(0xFFFBFDFA),
    surface = Color(0xFFFBFDFA),
    surfaceVariant = Color(0xFFDBE5E0),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AD6C3),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF005343),
    onPrimaryContainer = Color(0xFFA6F2DD),
    secondary = Color(0xFFB1CCC3),
    tertiary = Color(0xFFA8CBE2),
    background = Color(0xFF191C1B),
    surface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFF3F4945),
)

/** Màu điểm cộng / điểm trừ, dùng thống nhất toàn app. */
object ScoreColors {
    val positive: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF6BD98F) else Color(0xFF1B873F)
    val negative: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF39B9B) else Color(0xFFC02626)
}

@Composable
fun ChamDiemTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
