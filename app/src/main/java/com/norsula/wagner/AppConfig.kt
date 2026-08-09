package com.norsula.wagner

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf

object AppConfig {
    var debugMode = mutableStateOf(false)
    var comicClickCount = mutableIntStateOf(0)
    var lastCheckedNum = mutableIntStateOf(-1)
    const val prefix = "Віві та Вагнер - "

    fun load(context: Context) {
        val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        debugMode.value = BuildConfig.DEBUG && prefs.getBoolean("debugMode", false)
        comicClickCount.intValue = prefs.getInt("comicClickCount", 0)
        lastCheckedNum.intValue = prefs.getInt("lastCheckedNum", -1)
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("debugMode", debugMode.value)
            .putInt("comicClickCount", comicClickCount.intValue)
            .putInt("lastCheckedNum", lastCheckedNum.intValue)
            .apply()
    }
}

