package com.example.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.data.model.Asistencia
import com.example.login.ui.components.CampusTopBar
import com.example.login.ui.theme.Amber
import com.example.login.ui.theme.Green

@Composable
fun AsistenciasScreen(
    state: AsistenciasUiState,
    onLoad: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        CampusTopBar(title = "Asistencias", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "${state.items.size} registros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.items.isNotEmpty()) {
                val presentes = state.items.count { it.estado.equals("presente", ignoreCase = true) }
                val tardanzas = state.items.count { it.estado.equals("tarde", ignoreCase = true) }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$presentes presentes · $tardanzas tardanzas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))

            when {
            state.isLoading -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.height(36.dp))
            }

            state.error != null -> Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            state.items.isEmpty() -> Text(
                text = "Aún no hay registros de asistencia.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(state.items, key = { it.id }) { asistencia ->
                    AsistenciaRow(asistencia)
                }
            }
            }
        }
    }
}

@Composable
private fun AsistenciaRow(asistencia: Asistencia) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = asistencia.fecha,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (asistencia.curso.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = asistencia.curso,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            EstadoChip(estado = asistencia.estado)
        }
    }
}

@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val (label, color) = when (estado.lowercase()) {
        "presente" -> "Presente" to Green
        "aprobada", "aceptada" -> "Aceptada" to Green
        "pagado" -> "Pagado" to Green
        "completado" -> "Completado" to Green
        "en_curso" -> "En curso" to MaterialTheme.colorScheme.primary
        "tarde" -> "Tardanza" to Amber
        "falta" -> "Falta" to MaterialTheme.colorScheme.error
        "justificada" -> "Justificada" to MaterialTheme.colorScheme.primary
        "rechazada" -> "Rechazada" to MaterialTheme.colorScheme.error
        else -> "Pendiente" to Amber
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}
