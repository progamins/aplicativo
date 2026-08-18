package com.example.login.data

import android.content.Context
import android.content.SharedPreferences

/** Guarda la sesión (token JWT + usuario) localmente. */
object SessionManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_TOKEN, value).apply()
        }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) {
            prefs.edit().putString(KEY_USERNAME, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private const val KEY_TOKEN = "token"
    private const val KEY_USERNAME = "username"
}
