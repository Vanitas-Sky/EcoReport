package com.ejemplo.ecoreport.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.*

private val DarkColorScheme = darkColorScheme(
    primary = EcoTvGreenPrimary,
    secondary = EcoTvGreenSecondary,
    surface = EcoTvSurface,
    onSurface = EcoTvOnSurface,
    surfaceVariant = EcoTvSurfaceVariant,
    onSurfaceVariant = EcoTvOnSurface
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EcoReportTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
