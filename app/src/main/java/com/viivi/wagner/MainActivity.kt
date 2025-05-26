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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment


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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon


@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = "Головна") },
                    label = { Text("Головна") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(painterResource(R.drawable.ic_search), contentDescription = "Пошук") },
                    label = { Text("Пошук") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(painterResource(R.drawable.ic_info), contentDescription = "Інформація") },
                    label = { Text("Інформація") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomePage()
                1 -> SearchPage()
                2 -> InfoPage()
            }
        }
    }
}

@Composable
fun HomePage() {
    var comics by remember { mutableStateOf<List<Comic>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            comics = fetchComics()
        } catch (e: Exception) {
            error = "Помилка завантаження: ${e.localizedMessage}"
        }
    }

    when {
        comics == null && error == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Завантаження коміксу...")
            }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Невідома помилка")
            }
        }
        comics.isNullOrEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Немає коміксів")
            }
        }
        else -> {
            val comic = comics!!.first()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                val painter = rememberAsyncImagePainter(comic.image)
                Image(
                    painter = painter,
                    contentDescription = comic.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
                Text(text = comic.title, style = MaterialTheme.typography.titleLarge)
            }


        }
    }
}

@Composable
fun SearchPage() {
    // TODO: implement search UI
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Пошук коміксів - скоро тут буде")
    }
}

@Composable
fun InfoPage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Інформація про додаток")
    }
}




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
                WagnerTheme {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun ComicList(modifier: Modifier = Modifier) {
    var comics by remember { mutableStateOf<List<Comic>>(emptyList()) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        comics = fetchComics()
        Log.d("ComicList", "Fetched comics count: ${comics.size}")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(comics) { comic ->
            Column {
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = comic.image,
                            onError = { error ->
                                Log.e("ImageLoad", "Failed to load image: ${comic.proxiedImage}")
                            },
                            onSuccess = { Log.d("ImageLoad", "Image loaded successfully") }
                        ),
                        contentDescription = comic.title,
                        modifier = Modifier
                            .height(200.dp)
                            .widthIn(min = 300.dp), // set min width to allow horizontal scroll
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(text = comic.title)
            }
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
