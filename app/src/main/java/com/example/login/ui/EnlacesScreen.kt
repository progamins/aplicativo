package com.example.login.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.login.ui.components.CampusTopBar
import com.example.login.ui.components.MenuRowCard
import com.example.login.ui.theme.Amber
import com.example.login.ui.theme.Cyan
import com.example.login.ui.theme.Green
import com.example.login.ui.theme.Indigo
import com.example.login.ui.theme.Violet

private data class Enlace(
    val titulo: String,
    val descripcion: String,
    val url: String,
    val icon: ImageVector,
    val tint: Color,
)

// Áreas web oficiales del instituto (contactos reales del sistema iestp).
private val ENLACES: List<Enlace>
    @Composable get() = listOf(
    Enlace(
        titulo = "Sitio institucional",
        descripcion = "iestpsullana.edu.pe",
        url = "https://iestpsullana.edu.pe",
        icon = Icons.Filled.Language,
        tint = Indigo,
    ),
    Enlace(
        titulo = "Facebook oficial",
        descripcion = "@iestsullanaoficial",
        url = "https://www.facebook.com/iestsullanaoficial",
        icon = Icons.Filled.Facebook,
        tint = Violet,
    ),
    Enlace(
        titulo = "Instagram",
        descripcion = "@iestsullana",
        url = "https://www.instagram.com/iestsullana",
        icon = Icons.Filled.CameraAlt,
        tint = Cyan,
    ),
    Enlace(
        titulo = "Correo institucional",
        descripcion = "mesadepartes@iestpsullana.edu.pe",
        url = "mailto:mesadepartes@iestpsullana.edu.pe",
        icon = Icons.Filled.Email,
        tint = Amber,
    ),
    Enlace(
        titulo = "Atención telefónica",
        descripcion = "+51 073 458 018",
        url = "tel:+51073458018",
        icon = Icons.Filled.Phone,
        tint = Green,
    ),
)

@Composable
fun EnlacesScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CampusTopBar(title = "Enlaces", onBack = onBack)

        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "Acceso directo a las áreas web del instituto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            ENLACES.forEach { enlace ->
                MenuRowCard(
                    title = enlace.titulo,
                    subtitle = enlace.descripcion,
                    icon = enlace.icon,
                    tint = enlace.tint,
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(enlace.url)))
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
