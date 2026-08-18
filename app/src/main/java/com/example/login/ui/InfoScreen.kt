package com.example.login.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.ui.components.CampusTopBar
import com.example.login.ui.components.SectionHeader
import com.example.login.ui.theme.Indigo

private val TERMINOS = listOf(
    "Campus Virtual está destinado a estudiantes y docentes del IESTP Sullana.",
    "Los datos personales se utilizan únicamente con fines académicos y administrativos.",
    "La información de pagos, horarios y cursos proviene de los sistemas oficiales del instituto.",
    "El estudiante es responsable de mantener actualizados sus datos de contacto.",
    "Las justificaciones son revisadas por la administración; su aprobación sigue los criterios institucionales.",
    "Ante dudas o consultas, contacta a la mesa de partes: +51 073 458 018.",
)

@Composable
fun InfoScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CampusTopBar(title = "Info", onBack = onBack)

        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            SectionHeader("Acerca de")
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Campus Virtual es la aplicación oficial del Instituto de Educación " +
                        "Superior Tecnológico Público Sullana. Permite a estudiantes y docentes " +
                        "acceder a recursos académicos, horarios, evaluaciones y material educativo " +
                        "desde cualquier lugar y en cualquier momento, promoviendo una comunicación " +
                        "fluida entre la comunidad educativa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(Modifier.height(22.dp))
            SectionHeader("Términos y condiciones")
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    TERMINOS.forEachIndexed { index, termino ->
                        Row(verticalAlignment = Alignment.Top) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Indigo.copy(alpha = 0.14f),
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = termino,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        if (index < TERMINOS.lastIndex) Spacer(Modifier.height(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
