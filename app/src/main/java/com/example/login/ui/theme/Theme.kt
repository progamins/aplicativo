package com.example.login.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Identidad de marca (institucional, usado con moderación) ──
val Indigo = Color(0xFF4C1D95)
val Violet = Color(0xFF7C3AED)
val Cyan = Color(0xFF0E7490)
val CyanLight = Color(0xFF22D3EE)

// ── Colores semánticos (adaptados al tema para mantener contraste) ──
val Green: Color
    @Composable get() = if (isDarkTheme()) Color(0xFF4ADE80) else Color(0xFF15803D)
val Amber: Color
    @Composable get() = if (isDarkTheme()) Color(0xFFFBBF24) else Color(0xFFB45309)

@Composable
private fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

// ── Esquema claro (aspecto principal, el más cercano al PDF) ──
private val LightColors = lightColorScheme(
    primary = Color(0xFF4C1D95),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF6D28D9),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1EEFB),
    onSecondaryContainer = Color(0xFF3B1D7A),
    tertiary = Color(0xFF0E7490),
    onTertiary = Color.White,
    background = Color(0xFFF5F6FA),
    onBackground = Color(0xFF1A2333),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A2333),
    surfaceVariant = Color(0xFFE9ECF5),
    onSurfaceVariant = Color(0xFF56637E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF1F8),
    surfaceContainer = Color(0xFFEAEDF6),
    surfaceContainerHigh = Color(0xFFE4E8F3),
    surfaceContainerHighest = Color(0xFFDEE3F0),
    outline = Color(0xFF7A86A3),
    outlineVariant = Color(0xFFDFE4F0),
    error = Color(0xFFDC2626),
    onError = Color.White,
)

// ── Esquema oscuro (adaptación del diseño institucional, no otro diseño) ──
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF2E1065),
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFF8B5CF6),
    onSecondary = Color.White,
    background = Color(0xFF10141F),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF161C2B),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF222B40),
    onSurfaceVariant = Color(0xFF9AA6BF),
    surfaceContainerLowest = Color(0xFF0C1019),
    surfaceContainerLow = Color(0xFF141A28),
    surfaceContainer = Color(0xFF171E2F),
    surfaceContainerHigh = Color(0xFF1D2639),
    surfaceContainerHighest = Color(0xFF232D43),
    outline = Color(0xFF4A5674),
    outlineVariant = Color(0xFF2B3550),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
)

/** Tipografía legible, jerarquía clara, sin tracking negativo exagerado. */
private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp,
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
        fontSize = 14.sp,
        lineHeight = 20.sp,
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
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
    ),
)

/** Radios contenidos (máx. 20 dp) para un aspecto institucional sobrio. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

/** Único degradado de marca: elemento puntual de identidad (logo, avatar). */
val BrandGradient: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(Violet, Indigo),
        start = Offset.Zero,
        end = Offset.Infinite,
    )

@Composable
fun LoginTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
