package com.example.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.login.data.model.AdminJustificacion
import com.example.login.ui.theme.Green
import com.example.login.ui.theme.Surface as CardColor
import com.example.login.ui.theme.TextMuted
import com.example.login.ui.theme.TextPrimary

@Composable
fun AdminScreen(
    state: AdminJustificacionesUiState,
    onLoad: () -> Unit,
    onUpdate: (id: Long, estado: String) -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Panel de administración",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        val pendientes = state.items.count { it.estado == "pendiente" }
        Text(
            text = "${state.items.size} solicitudes · $pendientes pendientes",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.height(36.dp))
            }

            state.error != null && state.items.isEmpty() -> Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            state.items.isEmpty() -> Text(
                text = "No hay justificaciones de estudiantes todavía.",
                color = TextMuted,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items, key = { it.id }) { item ->
                    AdminJustificacionCard(
                        item = item,
                        onUpdate = onUpdate,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminJustificacionCard(
    item: AdminJustificacion,
    onUpdate: (id: Long, estado: String) -> Unit,
) {
    val pendiente = item.estado == "pendiente"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.motivo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${item.fullName.ifBlank { item.username }} · @${item.username}",
                        fontSize = 12.sp,
                        color = TextMuted,
                    )
                }
                EstadoChip(estado = item.estado)
            }
            Spacer(Modifier.height(6.dp))
            Text("Fecha: ${item.fecha}", fontSize = 12.sp, color = TextMuted)
            if (item.detalle.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(item.detalle, fontSize = 13.sp, color = TextMuted)
            }

            if (pendiente) {
                Spacer(Modifier.height(12.dp))
                Row {
                    Button(
                        onClick = { onUpdate(item.id, "aprobada") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green.copy(alpha = 0.18f),
                            contentColor = Green,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                    ) {
                        Icon(Icons.Filled.Check, null, modifier = Modifier.width(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aprobar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = { onUpdate(item.id, "rechazada") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                    ) {
                        Icon(Icons.Filled.Close, null, modifier = Modifier.width(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rechazar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
