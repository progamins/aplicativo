package com.example.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.login.data.SessionManager
import com.example.login.ui.AsistenciasScreen
import com.example.login.ui.AuthUiState
import com.example.login.ui.AuthViewModel
import com.example.login.ui.CuentaScreen
import com.example.login.ui.CursosScreen
import com.example.login.ui.EnlacesScreen
import com.example.login.ui.HorariosScreen
import com.example.login.ui.IdentificacionScreen
import com.example.login.ui.InfoScreen
import com.example.login.ui.JustificacionesScreen
import com.example.login.ui.LoginScreen
import com.example.login.ui.MenuScreen
import com.example.login.ui.PagosScreen
import com.example.login.ui.RegisterScreen
import com.example.login.ui.theme.LoginTheme

/** Destinos del menú principal (el Menú es la raíz; cada sección es una hoja). */
enum class CampusDest {
    Menu,
    Identificacion,
    Pagos,
    Horarios,
    Cursos,
    Enlaces,
    Justificaciones,
    Asistencias,
    Cuenta,
    Info,
}

private enum class Screen { Login, Register }

class MainActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(SessionManager.darkMode) }
            val toggleTheme: () -> Unit = {
                darkTheme = !darkTheme
                SessionManager.darkMode = darkTheme
            }
            LoginTheme(darkTheme = darkTheme) {
                AppRoot(
                    viewModel = viewModel,
                    darkTheme = darkTheme,
                    onToggleTheme = toggleTheme,
                )
            }
        }
    }
}

@Composable
private fun AppRoot(
    viewModel: AuthViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }
    var dest by rememberSaveable { mutableStateOf(CampusDest.Menu) }

    // Volver desde una sección siempre regresa al menú.
    BackHandler(enabled = dest != CampusDest.Menu) { dest = CampusDest.Menu }

    when {
        state.isLoggedIn -> CampusHome(
            viewModel = viewModel,
            state = state,
            darkTheme = darkTheme,
            onToggleTheme = onToggleTheme,
            dest = dest,
            onNavigate = { dest = it },
            onLogout = viewModel::logout,
        )

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
private fun CampusHome(
    viewModel: AuthViewModel,
    state: AuthUiState,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    dest: CampusDest,
    onNavigate: (CampusDest) -> Unit,
    onLogout: () -> Unit,
) {
    val profile by viewModel.profile.collectAsState()
    val pagos by viewModel.pagos.collectAsState()
    val horarios by viewModel.horarios.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val justificaciones by viewModel.justificaciones.collectAsState()
    val asistencias by viewModel.asistencias.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (dest) {
            CampusDest.Menu -> MenuScreen(
                username = state.username,
                fullName = state.fullName,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onOpen = onNavigate,
                onLogout = onLogout,
            )

            CampusDest.Identificacion -> IdentificacionScreen(
                username = state.username,
                fullName = state.fullName,
                dni = state.dni,
                programa = state.programa,
                state = profile,
                onLoad = viewModel::loadProfile,
                onSave = viewModel::updateProfile,
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Pagos -> PagosScreen(
                state = pagos,
                onLoad = viewModel::loadPagos,
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Horarios -> HorariosScreen(
                state = horarios,
                onLoad = viewModel::loadHorarios,
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Cursos -> CursosScreen(
                state = cursos,
                onLoad = viewModel::loadCursos,
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Enlaces -> EnlacesScreen(
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Justificaciones -> JustificacionesScreen(
                state = justificaciones,
                onLoad = viewModel::loadJustificaciones,
                onCreate = viewModel::createJustificacion,
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Asistencias -> AsistenciasScreen(
                state = asistencias,
                onLoad = viewModel::loadAsistencias,
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Cuenta -> CuentaScreen(
                username = state.username,
                fullName = state.fullName,
                email = profile.email,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onOpenIdentificacion = { onNavigate(CampusDest.Identificacion) },
                onLogout = onLogout,
                onBack = { onNavigate(CampusDest.Menu) },
            )

            CampusDest.Info -> InfoScreen(
                onBack = { onNavigate(CampusDest.Menu) },
            )
        }
    }
}
