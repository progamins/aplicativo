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
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.data.model.Curso
import com.example.login.ui.components.CampusTopBar
import com.example.login.ui.components.EmptyState
import com.example.login.ui.components.ErrorState
import com.example.login.ui.components.LoadingState
import com.example.login.ui.theme.Indigo

@Composable
fun CursosScreen(
    state: CursosUiState,
    onLoad: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { onLoad() }

    Column(Modifier.fillMaxSize()) {
        CampusTopBar(title = "Cursos", onBack = onBack)

        when {
            state.isLoading && state.items.isEmpty() -> LoadingState()

            state.error != null && state.items.isEmpty() -> ErrorState(state.error, onLoad)

            state.items.isEmpty() -> EmptyState(
                message = "Aún no tienes cursos asignados.",
                icon = Icons.Filled.School,
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(state.items, key = { it.id }) { curso ->
                    CursoRow(curso)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun CursoRow(curso: Curso) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Código del curso
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Indigo.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = curso.codigo,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Indigo,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = curso.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = curso.docente.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Créditos: ${curso.creditos}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(10.dp))
        EstadoChip(estado = curso.estado)
    }
}
