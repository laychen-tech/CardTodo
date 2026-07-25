package com.example.cardtodo

import android.app.Application

class CardTodoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsActivity.applyTheme(this)
    }
}
