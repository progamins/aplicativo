package com.example.login.data

import android.content.Context
import android.content.SharedPreferences

/** Guarda la sesión (access token, refresh token y datos del usuario) localmente. */
object SessionManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) {
            prefs.edit().putString(KEY_ACCESS, value).apply()
        }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) {
            prefs.edit().putString(KEY_REFRESH, value).apply()
        }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) {
            prefs.edit().putString(KEY_USERNAME, value).apply()
        }

    var fullName: String?
        get() = prefs.getString(KEY_FULLNAME, null)
        set(value) {
            prefs.edit().putString(KEY_FULLNAME, value).apply()
        }

    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) {
            prefs.edit().putString(KEY_EMAIL, value).apply()
        }

    var direccion: String?
        get() = prefs.getString(KEY_DIRECCION, null)
        set(value) {
            prefs.edit().putString(KEY_DIRECCION, value).apply()
        }

    var telefono: String?
        get() = prefs.getString(KEY_TELEFONO, null)
        set(value) {
            prefs.edit().putString(KEY_TELEFONO, value).apply()
        }

    var dni: String?
        get() = prefs.getString(KEY_DNI, null)
        set(value) {
            prefs.edit().putString(KEY_DNI, value).apply()
        }

    var programa: String?
        get() = prefs.getString(KEY_PROGRAMA, null)
        set(value) {
            prefs.edit().putString(KEY_PROGRAMA, value).apply()
        }

    /** Preferencia visual del usuario: modo noche activado o desactivado. Por
     *  defecto la app usa el tema claro (el más cercano al concepto del PDF). */
    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        }

    /** Ubicación de pago seleccionada ("ubicación automatizada"). Se guarda POR
     *  USUARIO: si en el mismo dispositivo entra otro alumno, ve su propia
     *  selección, no la del anterior. */
    var pagoUbicacion: String?
        get() = prefs.getString(pagoUbicacionKey(), null)
        set(value) {
            prefs.edit().putString(pagoUbicacionKey(), value).apply()
        }

    private fun pagoUbicacionKey(): String =
        "${KEY_PAGO_UBICACION}_${username.orEmpty().ifBlank { "guest" }}"

    fun clear() {
        prefs.edit().clear().apply()
    }

    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USERNAME = "username"
    private const val KEY_FULLNAME = "full_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_DIRECCION = "direccion"
    private const val KEY_TELEFONO = "telefono"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_PAGO_UBICACION = "pago_ubicacion"
    private const val KEY_DNI = "dni"
    private const val KEY_PROGRAMA = "programa"
}
