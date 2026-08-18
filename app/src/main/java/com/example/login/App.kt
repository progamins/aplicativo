package com.example.login

import android.app.Application

import com.example.login.data.SessionManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
