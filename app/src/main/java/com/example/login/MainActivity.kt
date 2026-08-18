package com.example.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.login.ui.AuthViewModel
import com.example.login.ui.HomeScreen
import com.example.login.ui.LoginScreen
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

@Composable
private fun AppRoot(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }

    when {
        state.isLoggedIn -> HomeScreen(
            username = state.username,
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
