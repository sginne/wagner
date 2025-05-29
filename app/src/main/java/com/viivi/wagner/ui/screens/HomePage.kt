package com.viivi.wagner.ui.screens

import android.content.Intent
import android.net.Uri

import com.viivi.wagner.utils.formatDate




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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable



import coil.compose.rememberAsyncImagePainter

import com.viivi.wagner.model.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput


//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
import com.viivi.wagner.ui.screens.devUi.DevPanel
import com.viivi.wagner.AppConfig

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.ui.unit.IntOffset

import androidx.compose.animation.ExperimentalAnimationApi


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomePage(selectedTab: (Int) -> Unit,
             onComicsLoaded: (List<Comic>)-> Unit,
             initialComicId: String? = null) {
    var comics by remember { mutableStateOf<List<Comic>?>(null) }
    var currentComic by remember { mutableStateOf<Comic?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var clicks by remember { mutableStateOf(AppConfig.comicClickCount.value) }
    //var debugMode = remember { mutableStateOf(true) } // в AppConfig
    var swipeDirection by remember { mutableStateOf(0) } // 1 = next, -1 = prev



    LaunchedEffect(Unit) {
        try {
            val loadedComics = fetchComicsWithCache(context)
            onComicsLoaded(loadedComics) // виклик callback, передаємо список коміксів в MainScreen
            comics = loadedComics
            currentComic = if (initialComicId != null) {
                comics?.find { it.id == initialComicId } ?: comics?.firstOrNull()
            } else {
                comics?.firstOrNull()
            }
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
            val refreshTrigger by remember { mutableStateOf(0) }

            LaunchedEffect(refreshTrigger) {
                try {
                    comics = fetchComicsWithCache(context)
                    val comicsList = comics
                    if (currentComic == null && !comicsList.isNullOrEmpty()) {
                        currentComic = comicsList.firstOrNull { it.id == initialComicId } ?: comicsList.firstOrNull()
                    }
                    error = null
                } catch (e: Exception) {
                    error = "Помилка оновлення: ${e.localizedMessage}"
                }
            }




            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(0.dp)
                    .pointerInput(currentComic, comics) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            if (dragAmount > 0) { // swipe right - NEXT comic
                                swipeDirection = 1
                                currentComic?.nextId?.let { nextId ->
                                    comics?.find { it.id == nextId }?.let {
                                        currentComic = it
                                    }
                                }
                            } else if (dragAmount < 0) { // swipe left - PREVIOUS comic
                                swipeDirection = -1
                                currentComic?.previousId?.let { prevId ->
                                    comics?.find { it.id == prevId }?.let {
                                        currentComic = it
                                    }
                                }
                            }
                            change.consume()
                        }
                    },
                verticalArrangement = Arrangement.Top
            ){
                if (AppConfig.debugMode.value) {
                    DevPanel()
                    Spacer(Modifier.height(0.dp))
                }

                AnimatedContent(
                    targetState = currentComic,
                    transitionSpec = {
                        val slideSpec = tween<IntOffset>(durationMillis = 300)
                        val fadeSpec = tween<Float>(durationMillis = 300)

                        if (swipeDirection == 1) {
                            (slideInHorizontally(animationSpec = slideSpec) { it } + fadeIn(animationSpec = fadeSpec)) with
                                    (slideOutHorizontally(animationSpec = slideSpec) { -it } + fadeOut(animationSpec = fadeSpec))
                        } else {
                            (slideInHorizontally(animationSpec = slideSpec) { -it } + fadeIn(animationSpec = fadeSpec)) with
                                    (slideOutHorizontally(animationSpec = slideSpec) { it } + fadeOut(animationSpec = fadeSpec))
                        }.using(SizeTransform(clip = false))
                    }
                ) { comic ->
                    comic?.let {
                        val painter = rememberAsyncImagePainter(it.image)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            it.nextTitle?.let { title ->
                                Text(
                                    text = "   <${title
                                        .removePrefix(AppConfig.prefix)
                                        .trim()
                                        .replace(" ", "<")}<",
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .width(5.dp)
                                )
                            }

                            Image(
                                painter = painter,
                                contentDescription = it.title,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        clicks++
                                        AppConfig.comicClickCount.value = clicks
                                        if (clicks >= 7) {
                                            AppConfig.debugMode.value = true
                                        }
                                    },
                                contentScale = ContentScale.Crop
                            )

                            it.previousTitle?.let { title ->
                                Text(
                                    text = "   <${title
                                        .removePrefix(AppConfig.prefix)
                                        .trim()
                                        .replace(" ", ">")}>",
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .width(5.dp)
                                )
                            }
                        }
                    }
                }



                Spacer(Modifier.height(0.dp))

                Text(
                    text = comic.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )

                val formattedDate = comic.publishedDate?.let { formatDate(it) } ?: "невідомо"
                Text(text = "переклад від $formattedDate")

                comic.id?.let { id ->
                    Text(
                        text = "Переглянути на сайті",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Blue),
                        modifier = Modifier
                            .padding(top = 0.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://norsula.com/$id"))
                                context.startActivity(intent)
                            }
                    )

                    Spacer(Modifier.height(0.dp))

                    Text(
                        text = "Поділитися",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Blue),
                        modifier = Modifier
                            .clickable {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, comic.title)
                                    putExtra(Intent.EXTRA_TEXT, "Подивись цей комікс: ${comic.title}\nhttps://norsula.com/$id")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Поділитися через"))
                            }
                    )
                }




                Spacer(Modifier.height(0.dp))
                val comicsSnapshot = comics
                val currentComicSnapshot = currentComic
                if (AppConfig.debugMode.value && comicsSnapshot != null && currentComicSnapshot != null) {
                    val position = comicsSnapshot.indexOfFirst { it.id == currentComicSnapshot.id }.takeIf { it >= 0 }?.let { comicsSnapshot.size - it } ?: "?"
                    val total = comicsSnapshot.size
                    Text(
                        text = "$position from $total",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(0.dp))
                }
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
                        .padding(top = 0.dp),
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




