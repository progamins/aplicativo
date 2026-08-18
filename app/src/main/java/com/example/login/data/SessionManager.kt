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

    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) {
            prefs.edit().putString(KEY_ROLE, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USERNAME = "username"
    private const val KEY_FULLNAME = "full_name"
    private const val KEY_ROLE = "role"
}
