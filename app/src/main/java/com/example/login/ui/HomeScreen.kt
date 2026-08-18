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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.ui.theme.Amber
import com.example.login.ui.theme.BrandGradient
import com.example.login.ui.theme.Green
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "¡Hola, $displayName! 👋",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Sistema académico · @$username",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
        )

        Spacer(Modifier.height(24.dp))

        // Resumen
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Pendientes",
                value = stats.pendientes,
                valueColor = Amber,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Presentes",
                value = stats.presentes,
                valueColor = Green,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Asistencias",
                value = stats.totalAsistencias,
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = "Accesos rápidos",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
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
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    .size(44.dp)
                    .background(BrandGradient, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}
