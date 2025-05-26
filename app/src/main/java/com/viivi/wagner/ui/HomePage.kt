package com.viivi.wagner.ui

import android.content.Intent
import android.net.Uri



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable



import coil.compose.rememberAsyncImagePainter
import com.viivi.wagner.model.Comic
import com.viivi.wagner.model.fetchComics

@Composable
fun HomePage(selectedTab: (Int) -> Unit) {
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
                comic.publishedDate?.let {
                    Text("Опубліковано: $it", style = MaterialTheme.typography.bodySmall)
                }

                comic.id?.let { id ->
                    val context = LocalContext.current
                    Text(
                        text = "Переглянути на сайті",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Blue),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://norsula.com/$id"))
                                context.startActivity(intent)
                            }
                    )
                }


                val totalPages = comics!!.size
                val currentPage = 1 // або зробити змінну, якщо хочеш гортати

                Text(
                    text = "Сторінка $currentPage / $totalPages",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { selectedTab(1) }) {
                        Text("Пошук")
                    }
                    TextButton(onClick = { selectedTab(2) }) {
                        Text("Інфо")
                    }
                }
            }
        }
    }
}


@Composable
fun SearchPage() {
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
