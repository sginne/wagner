package com.norsula.wagner.model

import android.util.Log
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL
import java.io.File
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString

@Serializable
data class WagnerInfo(
    val title: String,
    val content: String
)

suspend fun fetchWagnerInfo(): WagnerInfo? = withContext(Dispatchers.IO) {
    try {
        val url = "https://norsula.com/wp-json/custom/v1/wagner-api/"
        val jsonString = URL(url).readText()
        Log.d("fetchWagnerInfo", "JSON fetched: $jsonString")
        Json.decodeFromString<WagnerInfo>(jsonString)
    } catch (e: Exception) {
        Log.e("fetchWagnerInfo", "Error fetching Wagner info", e)
        null
    }
}

suspend fun fetchWagnerInfoWithCache(context: Context): WagnerInfo? = withContext(Dispatchers.IO) {
    val url = "https://norsula.com/wp-json/custom/v1/wagner-api/"
    val cacheDir = File(context.filesDir, "wagner_cache")
    if (!cacheDir.exists()) cacheDir.mkdirs()

    val cacheFile = File(cacheDir, "wagner_info.json")

    try {
        val jsonString = URL(url).readText()
        val info = Json.decodeFromString<WagnerInfo>(jsonString)

        // Save to cache
        cacheFile.writeText(jsonString)
        info
    } catch (e: Exception) {
        // fallback: load from cache if network fails
        if (cacheFile.exists()) {
            try {
                Json.decodeFromString<WagnerInfo>(cacheFile.readText())
            } catch (cacheE: Exception) {
                Log.e("fetchWagnerInfo", "Error reading cache", cacheE)
                null
            }
        } else {
            null
        }
    }
}