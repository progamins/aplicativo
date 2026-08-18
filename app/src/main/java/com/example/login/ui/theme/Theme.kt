package com.example.login.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF6D28D9)
private val Cyan = Color(0xFF0891B2)
private val Background = Color(0xFF0F172A)
private val Surface = Color(0xFF1E293B)
private val TextPrimary = Color(0xFFE2E8F0)
private val Error = Color(0xFFF87171)

private val DarkColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Color.White,
    secondary = Indigo,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    error = Error,
)

@Composable
fun LoginTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
