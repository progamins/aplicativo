package com.example.login.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.CampusDest
import com.example.login.ui.components.MenuCard
import com.example.login.ui.components.MenuRowCard
import com.example.login.ui.components.SectionHeader
import com.example.login.ui.theme.Amber
import com.example.login.ui.theme.BrandGradient
import com.example.login.ui.theme.Cyan
import com.example.login.ui.theme.Green
import com.example.login.ui.theme.Indigo
import com.example.login.ui.theme.Violet

@Composable
fun MenuScreen(
    username: String,
    fullName: String,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpen: (CampusDest) -> Unit,
    onLogout: () -> Unit,
) {
    val displayName = fullName.ifBlank { username }
    val initial = displayName.trim().firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Encabezado institucional compacto (sólido, sin degradado) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "CAMPUS VIRTUAL",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = "IESTP SULLANA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    )
                }
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (darkTheme) "Activar modo claro" else "Activar modo noche",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar con el degradado de identidad de la marca
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Hola, $displayName",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = "@$username",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            // ── Secciones primarias: tarjetas uniformes ──
            SectionHeader("Primarios")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MenuCard(
                    title = "Identificación",
                    subtitle = "Datos personales",
                    icon = Icons.Filled.Badge,
                    tint = Indigo,
                    onClick = { onOpen(CampusDest.Identificacion) },
                    modifier = Modifier.weight(1f),
                )
                MenuCard(
                    title = "Pagos",
                    subtitle = "Ubicaciones y estado",
                    icon = Icons.Filled.Payments,
                    tint = Cyan,
                    onClick = { onOpen(CampusDest.Pagos) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MenuCard(
                    title = "Horarios",
                    subtitle = "Clases semanales",
                    icon = Icons.Filled.Schedule,
                    tint = Violet,
                    onClick = { onOpen(CampusDest.Horarios) },
                    modifier = Modifier.weight(1f),
                )
                MenuCard(
                    title = "Cursos",
                    subtitle = "Tu plan de estudios",
                    icon = Icons.Filled.School,
                    tint = Amber,
                    onClick = { onOpen(CampusDest.Cursos) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Secciones secundarias ──
            SectionHeader("Agregados")
            Spacer(Modifier.height(10.dp))
            MenuRowCard(
                title = "Justificaciones",
                subtitle = "Registra y verifica tus justificaciones",
                icon = Icons.Filled.Description,
                tint = Indigo,
                onClick = { onOpen(CampusDest.Justificaciones) },
            )
            Spacer(Modifier.height(8.dp))
            MenuRowCard(
                title = "Asistencias",
                subtitle = "Historial de asistencia a clases",
                icon = Icons.Filled.CheckCircle,
                tint = Green,
                onClick = { onOpen(CampusDest.Asistencias) },
            )
            Spacer(Modifier.height(8.dp))
            MenuRowCard(
                title = "Enlaces",
                subtitle = "Áreas web del instituto",
                icon = Icons.Filled.Link,
                tint = Cyan,
                onClick = { onOpen(CampusDest.Enlaces) },
            )
            Spacer(Modifier.height(8.dp))
            MenuRowCard(
                title = "Cuenta",
                subtitle = "Preferencias y sesión",
                icon = Icons.Filled.Settings,
                tint = Violet,
                onClick = { onOpen(CampusDest.Cuenta) },
            )

            Spacer(Modifier.height(20.dp))

            // ── Información / acciones ──
            SectionHeader("Información")
            Spacer(Modifier.height(10.dp))
            MenuRowCard(
                title = "Info",
                subtitle = "Acerca de y términos",
                icon = Icons.Filled.Info,
                tint = Cyan,
                onClick = { onOpen(CampusDest.Info) },
            )
            Spacer(Modifier.height(8.dp))
            MenuRowCard(
                title = "Cerrar sesión",
                subtitle = "",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                tint = MaterialTheme.colorScheme.error,
                onClick = onLogout,
                destructive = true,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
