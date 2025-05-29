package com.norsula.wagner

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object AppConfig {
    var debugMode = mutableStateOf(true)
    var comicClickCount = mutableStateOf(0)
    const val prefix = "Віві та Вагнер - "

    fun load(context: Context) {
        val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        debugMode.value = prefs.getBoolean("debugMode", true)
        comicClickCount.value = prefs.getInt("comicClickCount", 0)
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("debugMode", debugMode.value)
            .putInt("comicClickCount", comicClickCount.value)
            .apply()
    }
}
