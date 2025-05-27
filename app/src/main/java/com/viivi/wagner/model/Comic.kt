package com.viivi.wagner.model

import android.util.Log
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import java.io.File
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString




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
    val nextTitle: String? = null,
    val num: Int? = null

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

suspend fun fetchComicsWithCache(context: Context): List<Comic> = withContext(Dispatchers.IO) {
    val url = "https://norsula.com/wp-json/custom/v1/comics/"
    val cacheDir = File(context.filesDir, "comics_cache")
    if (!cacheDir.exists()) cacheDir.mkdirs()

    try {
        val jsonString = URL(url).readText()
        val comicsJsonArray = Json.parseToJsonElement(jsonString).jsonArray

        val result = mutableListOf<Comic>()

        for (jsonElement in comicsJsonArray) {
            val comicObj = jsonElement.jsonObject
            val num = comicObj["num"]?.jsonPrimitive?.intOrNull ?: continue

            val comic = Json.decodeFromJsonElement<Comic>(comicObj)

            val comicDir = File(cacheDir, num.toString())
            val comicFile = File(comicDir, "comic.json")

            if (!comicFile.exists()) {
                comicDir.mkdirs()
                comicFile.writeText(Json.encodeToString(comic))
            }

            result.add(comic)
        }
        result
    } catch (e: Exception) {
        // fallback: load all from cache if network fails
        val cachedComics = cacheDir.listFiles()?.mapNotNull { dir ->
            File(dir, "comic.json").takeIf { it.exists() }?.readText()?.let {
                try {
                    Json.decodeFromString<Comic>(it)
                } catch (_: Exception) {
                    null
                }
            }
        } ?: emptyList()
        cachedComics
    }
}
