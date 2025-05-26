package com.viivi.wagner.model

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets

@Serializable
data class Comic(
    val title: String,
    val image: String,
    val publishedDate: String? = null,
    val facebookPage: String? = null,
    val id: String? = null,
    val previousId: String? = null,
    val previousTitle: String? = null,
    val nextId: String? = null,
    val nextTitle: String? = null

) {
    val proxiedImage: String
        get() = "https://norsula.com/wp-json/custom/v1/proxy/?url=${URLEncoder.encode(image, StandardCharsets.UTF_8.toString())}"
}

suspend fun fetchComics(): List<Comic> = withContext(Dispatchers.IO) {
    try {
        val url = "https://norsula.com/wp-json/custom/v1/comics/"
        val jsonString = URL(url).readText()
        Log.d("fetchComics", "JSON fetched: $jsonString")
        Json.decodeFromString(jsonString)
    } catch (e: Exception) {
        Log.e("fetchComics", "Error fetching comics", e)
        emptyList()
    }
}
