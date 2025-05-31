package com.norsula.wagner.utils

import android.util.Log
import com.norsula.wagner.AppConfig

object LogUtil {
    private const val TAG = "debuWagner"

    private val isDebug get() = AppConfig.debugMode.value

    @JvmStatic
    fun debug(msg: String) {
        if (isDebug) Log.d(TAG, msg)
    }

    @JvmStatic
    fun info(msg: String) {
        Log.i(TAG, msg)
    }

    @JvmStatic
    fun error(msg: String, e: Exception? = null) {
        Log.e(TAG, msg, e)
    }

    // Add this for comic-specific logging
    @JvmStatic
    fun comic(msg: String) {
        if (isDebug) Log.d("$TAG-Comic", msg)
    }
}