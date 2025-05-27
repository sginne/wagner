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

import com.viivi.wagner.model.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign

import java.time.LocalDate
import java.time.format.DateTimeFormatter





@Composable
fun HomePage(selectedTab: (Int) -> Unit) {
    var comics by remember { mutableStateOf<List<Comic>?>(null) }
    var currentComic by remember { mutableStateOf<Comic?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        try {
            comics = fetchComicsWithCache(context)
            currentComic = comics?.firstOrNull()
        } catch (e: Exception) {
            error = "Помилка завантаження: ${e.localizedMessage}"
        }
    }

    when {
        comics == null && error == null -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Завантаження коміксу...")
            }
        }
        error != null -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(error ?: "Невідома помилка")
            }
        }
        comics.isNullOrEmpty() -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Немає коміксів")
            }
        }
        else -> {
            val comic = currentComic!!
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dev Panel", fontWeight = FontWeight.Bold)
                    var refreshTrigger by remember { mutableStateOf(0) }

                    TextButton(onClick = {
                        refreshTrigger++
                    }) {
                        Text("Оновити")
                    }

                    LaunchedEffect(refreshTrigger) {
                        try {
                            comics = fetchComicsWithCache(context)
                            currentComic = comics?.firstOrNull()
                            error = null
                        } catch (e: Exception) {
                            error = "Помилка оновлення: ${e.localizedMessage}"
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Далі твій існуючий код з картинкою, текстом і тд
            }

            Column(
                Modifier
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

                Spacer(Modifier.height(1.dp))

                Text(
                    text = comic.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
                comic.publishedDate?.let { dateStr ->
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = LocalDate.parse(dateStr, formatter)

                    val day = date.dayOfMonth
                    val month = when (date.monthValue) {
                        1 -> "січня"
                        2 -> "лютого"
                        3 -> "березня"
                        4 -> "квітня"
                        5 -> "травня"
                        6 -> "червня"
                        7 -> "липня"
                        8 -> "серпня"
                        9 -> "вересня"
                        10 -> "жовтня"
                        11 -> "листопада"
                        12 -> "грудня"
                        else -> ""
                    }
                    val year = date.year

                    Text(
                        text = "переклад від $day $month $year",
                        style = MaterialTheme.typography.bodySmall
                    )
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

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (comic.previousId != null) {
                        TextButton(onClick = {
                            val newComic = comics!!.find { it.id == comic.previousId }
                            if (newComic != null) currentComic = newComic
                        }) {
                            Text("${comic.previousTitle}")
                        }
                    }
                    if (comic.nextId != null) {
                        TextButton(onClick = {
                            val newComic = comics!!.find { it.id == comic.nextId }
                            if (newComic != null) currentComic = newComic
                        }) {
                            Text("${comic.nextTitle}")
                        }
                    }
                }

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
