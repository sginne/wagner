package com.viivi.wagner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.viivi.wagner.theme.WagnerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL
import androidx.compose.ui.res.painterResource
import android.util.Log
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Serializable
data class Comic(val title: String, val image: String) {
    // Add a computed property for proxied image URL
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            WagnerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Green)
                            .padding(innerPadding)
                    ) {
                        ComicList()
                    }
                }
            }
        }
    }
}

@Composable
fun ComicList(modifier: Modifier = Modifier) {
    var comic by remember { mutableStateOf<Comic?>(null) }
    val scope = rememberCoroutineScope()
    if (comic == null) {
        Log.d("ComicList", "No comic to display")
    }

    LaunchedEffect(Unit) {
        val comics = fetchComics()
        Log.d("ComicList", "Fetched comics count: ${comics.size}")
        comic = comics.firstOrNull()
    }

    comic?.let {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = it.image,
                    onError = { error ->
                        Log.e("ImageLoad", "Failed to load image: ${error.result.throwable}")
                    },
                    onSuccess = { Log.d("ImageLoad", "Image loaded successfully") }
                ),
                contentDescription = it.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.FillWidth
            )
            Spacer(Modifier.height(8.dp))
            Text(text = it.title)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ComicListPreview() {
    WagnerTheme {
        ComicList()
    }
}
