package com.example.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.login.ui.AdminScreen
import com.example.login.ui.AsistenciasScreen
import com.example.login.ui.AuthUiState
import com.example.login.ui.AuthViewModel
import com.example.login.ui.HomeScreen
import com.example.login.ui.JustificacionesScreen
import com.example.login.ui.LoginScreen
import com.example.login.ui.PerfilScreen
import com.example.login.ui.RegisterScreen
import com.example.login.ui.theme.LoginTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginTheme {
                AppRoot(viewModel)
            }
        }
    }
}

private enum class Screen { Login, Register }

private enum class Tab(val label: String, val icon: ImageVector) {
    Inicio("Inicio", Icons.Filled.Home),
    Justificaciones("Justificaciones", Icons.Filled.Description),
    Asistencias("Asistencias", Icons.Filled.CheckCircle),
    Admin("Admin", Icons.Filled.AdminPanelSettings),
    Perfil("Perfil", Icons.Filled.Person),
}

@Composable
private fun AppRoot(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }
    var tab by rememberSaveable { mutableStateOf(Tab.Inicio) }

    when {
        state.isLoggedIn -> {
            // Si la sesión deja de ser admin, nunca quedarse en el tab Admin.
            LaunchedEffect(state.isAdmin) {
                if (tab == Tab.Admin && !state.isAdmin) tab = Tab.Inicio
            }
            MainScaffold(
                viewModel = viewModel,
                state = state,
                tab = tab,
                onTabChange = { tab = it },
            )
        }

        screen == Screen.Register -> RegisterScreen(
            isLoading = state.isLoading,
            error = state.error,
            onRegister = viewModel::register,
            onBack = { screen = Screen.Login },
        )

        else -> LoginScreen(
            isLoading = state.isLoading,
            error = state.error,
            onLogin = viewModel::login,
            onGoRegister = { screen = Screen.Register },
        )
    }
}

@Composable
private fun MainScaffold(
    viewModel: AuthViewModel,
    state: AuthUiState,
    tab: Tab,
    onTabChange: (Tab) -> Unit,
) {
    val stats by viewModel.stats.collectAsState()
    val justificaciones by viewModel.justificaciones.collectAsState()
    val asistencias by viewModel.asistencias.collectAsState()
    val adminJustificaciones by viewModel.adminJustificaciones.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                // El tab Admin solo aparece para usuarios con rol administrador.
                Tab.entries
                    .filter { it != Tab.Admin || state.isAdmin }
                    .forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { onTabChange(item) },
                            icon = { Icon(item.icon, null) },
                            label = { Text(item.label) },
                        )
                    }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (tab) {
                Tab.Inicio -> HomeScreen(
                    username = state.username,
                    fullName = state.fullName,
                    stats = stats,
                    onOpenJustificaciones = { onTabChange(Tab.Justificaciones) },
                    onOpenAsistencias = { onTabChange(Tab.Asistencias) },
                )

                Tab.Justificaciones -> JustificacionesScreen(
                    state = justificaciones,
                    onLoad = viewModel::loadJustificaciones,
                    onCreate = viewModel::createJustificacion,
                )

                Tab.Asistencias -> AsistenciasScreen(
                    state = asistencias,
                    onLoad = viewModel::loadAsistencias,
                )

                Tab.Admin -> AdminScreen(
                    state = adminJustificaciones,
                    onLoad = viewModel::loadAdminJustificaciones,
                    onUpdate = viewModel::updateJustificacionEstado,
                )

                Tab.Perfil -> PerfilScreen(
                    username = state.username,
                    fullName = state.fullName,
                    isAdmin = state.isAdmin,
                    onLogout = viewModel::logout,
                )
            }
        }
    }
}
