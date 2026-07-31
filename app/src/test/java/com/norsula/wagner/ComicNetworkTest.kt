package com.norsula.wagner.model

import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ComicNetworkTest {

    @Test
    fun `successful response returns comics`() {
        val url = fakeUrl(
            status = 200,
            body = """[{"title":"First","image":"first.jpg","num":1}]"""
        )

        val comics = fetchComicsFromUrl(url)

        assertEquals(1, comics.size)
        assertEquals("First", comics.single().title)
        assertEquals(1, comics.single().num)
    }

    @Test
    fun `http error throws IOException`() {
        val url = fakeUrl(status = 503, body = "Unavailable")

        val error = assertThrows(IOException::class.java) {
            fetchComicsFromUrl(url)
        }

        assertEquals("HTTP 503", error.message)
    }

    @Test
    fun `invalid json throws exception`() {
        val url = fakeUrl(status = 200, body = "{broken")

        assertThrows(Exception::class.java) {
            fetchComicsFromUrl(url)
        }
    }

    private fun fakeUrl(status: Int, body: String): URL =
        URL(null, "http://test/comics", object : URLStreamHandler() {
            override fun openConnection(url: URL): URLConnection =
                object : HttpURLConnection(url) {
                    override fun getResponseCode(): Int = status

                    override fun getInputStream() =
                        ByteArrayInputStream(body.toByteArray())

                    override fun disconnect() = Unit
                    override fun usingProxy(): Boolean = false
                    override fun connect() = Unit
                }
        })
}
