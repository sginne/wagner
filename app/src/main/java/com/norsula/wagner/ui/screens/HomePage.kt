package com.norsula.wagner.ui.screens

import com.norsula.wagner.BuildConfig




import android.content.Intent
import android.net.Uri

import com.norsula.wagner.utils.formatDate




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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable



import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest

import com.norsula.wagner.model.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch


//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
import com.norsula.wagner.ui.screens.devUi.DevPanel
import com.norsula.wagner.AppConfig

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.ui.unit.IntOffset

import androidx.compose.animation.ExperimentalAnimationApi


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomePage(selectedTab: (Int) -> Unit,
             onComicsLoaded: (List<Comic>)-> Unit,
             initialComicId: String? = null,
             cachedComics: List<Comic>? = null) {
    var comics by remember { mutableStateOf(cachedComics) }
    var currentComic by remember {
        mutableStateOf(
            cachedComics?.find { it.id == initialComicId }
                ?: cachedComics?.firstOrNull()
        )
    }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var clicks by remember { mutableIntStateOf(AppConfig.comicClickCount.intValue) }
    //var debugMode = remember { mutableStateOf(true) } // в AppConfig
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var sliderWidth by remember { mutableFloatStateOf(1f) }
    val sliderScope = rememberCoroutineScope()



    LaunchedEffect(Unit) {
        if (comics != null) return@LaunchedEffect

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

    LaunchedEffect(currentComic, comics) {
        val loadedComics = comics.orEmpty()
        val comic = currentComic ?: return@LaunchedEffect

        listOfNotNull(comic.previousId, comic.nextId)
            .distinct()
            .mapNotNull { id -> loadedComics.find { it.id == id } }
            .forEach { neighbour ->
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(neighbour.image)
                        .build()
                )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(0.dp),
                verticalArrangement = Arrangement.Top
            ){
                if (BuildConfig.DEBUG && AppConfig.debugMode.value) {
                    DevPanel(comics.orEmpty())
                    Spacer(Modifier.height(0.dp))
                }

                val loadedComics = comics.orEmpty()

                fun liveDrag(): Modifier = Modifier.pointerInput(
                    currentComic,
                    loadedComics,
                    sliderWidth
                ) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragOffset = 0f },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            val active = currentComic
                            val minimum =
                                if (active?.previousId != null) -sliderWidth else 0f
                            val maximum =
                                if (active?.nextId != null) sliderWidth else 0f
                            dragOffset = (dragOffset + amount).coerceIn(minimum, maximum)
                        },
                        onDragEnd = {
                            sliderScope.launch {
                                val active = currentComic
                                val direction = when {
                                    dragOffset > sliderWidth * 0.15f -> 1
                                    dragOffset < -sliderWidth * 0.15f -> -1
                                    else -> 0
                                }

                                val targetId = when (direction) {
                                    1 -> active?.nextId
                                    -1 -> active?.previousId
                                    else -> null
                                }

                                val target = targetId?.let { id ->
                                    loadedComics.find { it.id == id }
                                }

                                val animation = Animatable(dragOffset)
                                animation.animateTo(
                                    if (target == null) 0f
                                    else direction * sliderWidth,
                                    tween(220)
                                ) {
                                    dragOffset = value
                                }

                                if (target != null) currentComic = target
                                dragOffset = 0f
                            }
                        },
                        onDragCancel = {
                            sliderScope.launch {
                                val animation = Animatable(dragOffset)
                                animation.animateTo(0f, tween(180)) {
                                    dragOffset = value
                                }
                                dragOffset = 0f
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .onSizeChanged {
                            if (it.width > 0) sliderWidth = it.width.toFloat()
                        }
                        .then(liveDrag())
                ) {
                    val active = currentComic
                    val next = active?.nextId?.let { id ->
                        loadedComics.find { it.id == id }
                    }
                    val previous = active?.previousId?.let { id ->
                        loadedComics.find { it.id == id }
                    }

                    listOf(
                        next to -sliderWidth,
                        active to 0f,
                        previous to sliderWidth
                    ).forEach { (item, position) ->
                        item?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it.image),
                                contentDescription = it.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationX = position + dragOffset
                                    }
                                    .clickable {
                                        clicks++
                                        AppConfig.comicClickCount.intValue = clicks
                                        if (BuildConfig.DEBUG && clicks >= 7) {
                                            AppConfig.debugMode.value = true
                                        }
                                    },
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }


                Spacer(Modifier.height(0.dp))
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = comic.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )

                        val formattedDate =
                            comic.publishedDate?.let { formatDate(it) } ?: "невідомо"

                        Text(
                            text = "переклад від $formattedDate",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                            ),
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://norsula.com/${comic.id}")
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = "Переглянути на сайті",
                                    fontFamily =
                                        androidx.compose.ui.text.font.FontFamily.Default
                                )
                            }

                            val comicsSnapshot = comics
                            val currentComicSnapshot = currentComic

                            if (
                                AppConfig.debugMode.value &&
                                comicsSnapshot != null &&
                                currentComicSnapshot != null
                            ) {
                                val position = comicsSnapshot
                                    .indexOfFirst {
                                        it.id == currentComicSnapshot.id
                                    }
                                    .takeIf { it >= 0 }
                                    ?.let { comicsSnapshot.size - it }
                                    ?: "?"

                                Text(
                                    text = "$position з ${comicsSnapshot.size}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily =
                                            androidx.compose.ui.text.font.FontFamily.Default
                                    )
                                )
                            }

                            TextButton(
                                onClick = {
                                    val shareIntent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                comic.title
                                            )
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Подивись цей комікс: ${comic.title}\n" +
                                                    "https://norsula.com/${comic.id}"
                                            )
                                        }

                                    context.startActivity(
                                        Intent.createChooser(
                                            shareIntent,
                                            "Поділитися через"
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = "Поділитися",
                                    fontFamily =
                                        androidx.compose.ui.text.font.FontFamily.Default
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .then(liveDrag())
                ) {
                    val active = currentComic

                    val next = active?.nextId?.let { id ->
                        loadedComics.find { it.id == id }
                    }
                    val previous = active?.previousId?.let { id ->
                        loadedComics.find { it.id == id }
                    }

                    val nextNext = next?.nextId?.let { id ->
                        loadedComics.find { it.id == id }
                    }
                    val previousPrevious = previous?.previousId?.let { id ->
                        loadedComics.find { it.id == id }
                    }

                    val halfWidth = sliderWidth / 2f

                    listOf(
                        nextNext to -halfWidth,
                        next to 0f,
                        previous to halfWidth,
                        previousPrevious to sliderWidth
                    ).forEach { (item, position) ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(with(LocalDensity.current) {
                                    halfWidth.toDp()
                                })
                                .graphicsLayer {
                                    translationX = position + dragOffset / 2f
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item != null) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(item.image),
                                        contentDescription = item.title,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = item.title.removePrefix("Віві та Вагнер – "),
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Гортайте комікси свайпом",
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "← Проведіть пальцем",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                        }
                    }
                }

                /*Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                }*/
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { selectedTab(1) }) {
                            Text(
                                text = "Пошук",
                                fontFamily =
                                    androidx.compose.ui.text.font.FontFamily.Default
                            )
                        }
                        TextButton(onClick = { selectedTab(2) }) {
                            Text(
                                text = "Інфо",
                                fontFamily =
                                    androidx.compose.ui.text.font.FontFamily.Default
                            )
                        }
                    }
                }
            }
        }
    }
}




