package com.example.login.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta de marca (indigo → cyan)
val Indigo = Color(0xFF6D28D9)
val Violet = Color(0xFF8B5CF6)
val Cyan = Color(0xFF0891B2)
val CyanLight = Color(0xFF22D3EE)

// Fondos y superficies
val Background = Color(0xFF0B1120)
val Surface = Color(0xFF131C31)
val SurfaceVariant = Color(0xFF1E293B)
val TextPrimary = Color(0xFFE2E8F0)
val TextMuted = Color(0xFF94A3B8)
val ErrorRed = Color(0xFFF87171)
val Amber = Color(0xFFFBBF24)
val Green = Color(0xFF4ADE80)

private val DarkColors = darkColorScheme(
    primary = CyanLight,
    onPrimary = Color(0xFF062A33),
    secondary = Violet,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
)

/** Gradiente de marca para fondos, botones y acentos. */
val BrandGradient: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(Violet, Indigo, Cyan),
        start = Offset.Zero,
        end = Offset.Infinite,
    )

/** Fondo vertical oscuro usado en las pantallas de auth. */
val AuthBackground: Brush
    @Composable get() = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B1120), Color(0xFF111A33), Color(0xFF0B1120)),
    )

@Composable
fun LoginTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
