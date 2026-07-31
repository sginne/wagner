package com.norsula.wagner.model

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
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
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
    val url = URL("https://norsula.com/wp-json/custom/v1/comics/")
    val cacheDir = File(context.filesDir, "comics_cache")

    if (!cacheDir.exists() && !cacheDir.mkdirs()) {
        throw java.io.IOException("Не вдалося створити каталог кешу")
    }

    try {
        val comics = fetchComicsFromUrl(url)

        comics.forEach { comic ->
            val num = comic.num ?: return@forEach
            val comicDir = File(cacheDir, num.toString())

            if (!comicDir.exists() && !comicDir.mkdirs()) {
                throw java.io.IOException("Не вдалося створити кеш для коміксу $num")
            }

            val comicFile = File(comicDir, "comic.json")
            val temporaryFile = File(comicDir, "comic.json.tmp")

            try {
                temporaryFile.writeText(Json.encodeToString(comic))

                try {
                    Files.move(
                        temporaryFile.toPath(),
                        comicFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(
                        temporaryFile.toPath(),
                        comicFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            } catch (cacheError: Exception) {
                Log.e("fetchComics", "Failed to cache comic $num", cacheError)
            } finally {
                temporaryFile.delete()
            }
        }

        Log.d("fetchComics", "Loaded ${comics.size} comics from network")
        comics
    } catch (networkError: Exception) {
        Log.e("fetchComics", "Network loading failed, trying cache", networkError)

        val cachedComics = loadCachedComics(cacheDir)

        if (cachedComics.isEmpty()) {
            throw java.io.IOException(
                "Не вдалося завантажити комікси, кеш порожній",
                networkError
            )
        }

        Log.d("fetchComics", "Loaded ${cachedComics.size} comics from cache")
        cachedComics
    }
}


internal fun loadCachedComics(
    cacheDir: File,
    logInvalidFile: (String, Exception) -> Unit = { path, error ->
        Log.e("fetchComics", "Invalid cache file: $path", error)
    }
): List<Comic> =
    cacheDir.listFiles()
        ?.mapNotNull { dir ->
            val comicFile = File(dir, "comic.json")
            if (!comicFile.isFile) return@mapNotNull null

            try {
                Json.decodeFromString<Comic>(comicFile.readText())
            } catch (cacheError: Exception) {
                logInvalidFile(comicFile.path, cacheError)
                null
            }
        }
        ?.sortedByDescending { it.num ?: Int.MIN_VALUE }
        .orEmpty()


internal fun fetchComicsFromUrl(url: URL): List<Comic> {
    val connection = url.openConnection() as java.net.HttpURLConnection

    return try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.requestMethod = "GET"

        val status = connection.responseCode
        if (status !in 200..299) {
            throw java.io.IOException("HTTP $status")
        }

        val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
        Json.decodeFromString(jsonString)
    } finally {
        connection.disconnect()
    }
}
