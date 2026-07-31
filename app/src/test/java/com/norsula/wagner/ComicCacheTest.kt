package com.norsula.wagner

import com.norsula.wagner.model.Comic
import com.norsula.wagner.model.fetchOrLoadCachedComics
import com.norsula.wagner.model.loadCachedComics
import java.io.IOException
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ComicCacheTest {
    @Test
    fun loadsValidComicsNewestFirstAndSkipsInvalidFiles() {
        val cacheDir = java.nio.file.Files.createTempDirectory("wagner-comics-").toFile()

        try {
            writeComic(cacheDir, Comic("Older", "older.jpg", num = 10))
            writeComic(cacheDir, Comic("Newer", "newer.jpg", num = 20))

            File(cacheDir, "15").mkdirs()
            File(cacheDir, "15/comic.json").writeText("broken json")

            val comics = loadCachedComics(cacheDir) { _, _ -> }

            assertEquals(listOf(20, 10), comics.map { it.num })
            assertEquals(listOf("Newer", "Older"), comics.map { it.title })
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun emptyDirectoryReturnsEmptyList() {
        val cacheDir = java.nio.file.Files.createTempDirectory("wagner-comics-").toFile()

        try {
            assertEquals(emptyList<Comic>(), loadCachedComics(cacheDir))
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun networkFailureReturnsCachedComics() {
        val cacheDir = java.nio.file.Files.createTempDirectory("wagner-comics-").toFile()

        try {
            writeComic(cacheDir, Comic("Cached", "cached.jpg", num = 12))

            val comics = fetchOrLoadCachedComics(
                cacheDir = cacheDir,
                fetch = { throw IOException("offline") },
                logError = { _, _ -> }
            )

            assertEquals(listOf(12), comics.map { it.num })
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun networkFailureWithEmptyCacheThrowsIOException() {
        val cacheDir = java.nio.file.Files.createTempDirectory("wagner-comics-").toFile()

        try {
            org.junit.Assert.assertThrows(IOException::class.java) {
                fetchOrLoadCachedComics(
                    cacheDir = cacheDir,
                    fetch = { throw IOException("offline") },
                    logError = { _, _ -> }
                )
            }
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    private fun writeComic(cacheDir: File, comic: Comic) {
        val comicDir = File(cacheDir, comic.num.toString())
        comicDir.mkdirs()
        File(comicDir, "comic.json").writeText(Json.encodeToString(comic))
    }
}
