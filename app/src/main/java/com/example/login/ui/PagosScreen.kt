package com.example.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.data.SessionManager
import com.example.login.data.model.Pago
import com.example.login.ui.components.CampusTopBar
import com.example.login.ui.components.EmptyState
import com.example.login.ui.components.ErrorState
import com.example.login.ui.components.LoadingState
import com.example.login.ui.theme.Amber
import com.example.login.ui.theme.Green

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagosScreen(
    state: PagosUiState,
    onLoad: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }

    var expanded by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(SessionManager.pagoUbicacion ?: "") }

    // Ubicación automatizada: se preselecciona la primera disponible la primera vez.
    LaunchedEffect(state.ubicaciones) {
        if (selected.isBlank() && state.ubicaciones.isNotEmpty()) {
            selected = state.ubicaciones.first()
            SessionManager.pagoUbicacion = selected
        }
    }

    Column(Modifier.fillMaxSize()) {
        CampusTopBar(title = "Pagos", onBack = onBack)

        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "Selecciona la ubicación de pago",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = selected.ifBlank { "Todas las ubicaciones" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ubicación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las ubicaciones") },
                        onClick = {
                            selected = ""
                            SessionManager.pagoUbicacion = null
                            expanded = false
                        },
                    )
                    state.ubicaciones.forEach { ubicacion ->
                        DropdownMenuItem(
                            text = { Text(ubicacion) },
                            onClick = {
                                selected = ubicacion
                                SessionManager.pagoUbicacion = ubicacion
                                expanded = false
                            },
                        )
                    }
                }
            }
        }

        when {
            state.isLoading && state.items.isEmpty() -> LoadingState()

            state.error != null && state.items.isEmpty() -> ErrorState(state.error, onLoad)

            state.items.isEmpty() -> EmptyState(
                message = "Aún no tienes pagos registrados.",
                icon = Icons.Filled.Payments,
            )

            else -> {
                val visible = if (selected.isBlank()) {
                    state.items
                } else {
                    state.items.filter { it.ubicacion == selected }
                }
                if (visible.isEmpty()) {
                    EmptyState(
                        message = "No hay pagos en esta ubicación.",
                        icon = Icons.Filled.Payments,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        items(visible, key = { it.id }) { pago ->
                            PagoRow(pago)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PagoRow(pago: Pago) {
    val pagado = pago.estado.equals("pagado", ignoreCase = true)
    val tint = if (pagado) Green else Amber

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (pagado) Icons.AutoMirrored.Filled.ReceiptLong else Icons.Filled.HourglassEmpty,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = pago.concepto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = buildString {
                        if (pago.ubicacion.isNotBlank()) append(pago.ubicacion)
                        if (pago.ubicacion.isNotBlank() && pago.fecha.isNotBlank()) append(" · ")
                        append(pago.fecha)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "S/ ${"%.2f".format(pago.monto)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            EstadoChip(estado = pago.estado)
        }
    }
}
