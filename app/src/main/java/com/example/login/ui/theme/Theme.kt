package com.example.login.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    tertiary = Cyan,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
)

/** Tipografía consistente: encabezados con peso alto y tracking ajustado. */
private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
    ),
)

/** Radios consistentes para tarjetas, campos y diálogos. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Gradiente de marca para fondos, botones y acentos. */
val BrandGradient: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(Violet, Indigo, Cyan),
        start = Offset.Zero,
        end = Offset.Infinite,
    )

/** Gradiente suave para el header del dashboard. */
val HeroGradient: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF164E63)),
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
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
