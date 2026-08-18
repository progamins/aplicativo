package com.example.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.ui.theme.Amber
import com.example.login.ui.theme.BrandGradient
import com.example.login.ui.theme.Green
import com.example.login.ui.theme.HeroGradient
import com.example.login.ui.theme.Surface as CardColor
import com.example.login.ui.theme.TextMuted
import com.example.login.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    username: String,
    fullName: String,
    stats: StatsUiState,
    onOpenJustificaciones: () -> Unit,
    onOpenAsistencias: () -> Unit,
) {
    val displayName = fullName.ifBlank { username }
    val initial = displayName.trim().firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header con gradiente ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroGradient),
        ) {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 26.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BrandGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initial,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Hola, $displayName",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "@$username · ${fechaHoy()}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(18.dp))

            // ── Resumen de estadísticas ──
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Pendientes",
                    value = stats.pendientes,
                    icon = Icons.Filled.PendingActions,
                    tint = Amber,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Presentes",
                    value = stats.presentes,
                    icon = Icons.Filled.CheckCircle,
                    tint = Green,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Asistencias",
                    value = stats.totalAsistencias,
                    icon = Icons.Filled.EventAvailable,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Accesos rápidos",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))

            QuickAction(
                title = "Justificaciones",
                subtitle = "${stats.justificaciones} registradas · ${stats.pendientes} pendientes",
                icon = Icons.Filled.Description,
                onClick = onOpenJustificaciones,
            )
            Spacer(Modifier.height(12.dp))
            QuickAction(
                title = "Asistencias",
                subtitle = "${stats.totalAsistencias} registros de asistencia",
                icon = Icons.Filled.CheckCircle,
                onClick = onOpenAsistencias,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

private val MESES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)
private val DIAS = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")

private fun fechaHoy(): String {
    val hoy = java.time.LocalDate.now()
    return "${DIAS[hoy.dayOfWeek.value % 7]}, ${hoy.dayOfMonth} de ${MESES[hoy.monthValue - 1]}"
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(BrandGradient, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted,
            )
        }
    }
}
