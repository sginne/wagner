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
import com.norsula.wagner.ui.components.SiteActionsStrip
import com.norsula.wagner.ui.components.UtilityActionsStrip

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
    var reloadKey by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var clicks by remember { mutableIntStateOf(AppConfig.comicClickCount.intValue) }
    //var debugMode = remember { mutableStateOf(true) } // в AppConfig
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var sliderWidth by remember { mutableFloatStateOf(1f) }
    val sliderScope = rememberCoroutineScope()



    LaunchedEffect(reloadKey) {
        if (comics != null) return@LaunchedEffect

        error = null
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    error ?: "Невідома помилка",
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = { reloadKey++ }) {
                    Text("Повторити")
                }
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
                val debugPositionText =
                    if (AppConfig.debugMode.value) {
                        val snapshot = comics.orEmpty()
                        val index = snapshot.indexOfFirst { it.id == comic.id }
                        val position = index
                            .takeIf { it >= 0 }
                            ?.let { snapshot.size - it }
                            ?: "?"
                        "$position з ${snapshot.size}"
                    } else {
                        null
                    }

                SiteActionsStrip(
                    comic = comic,
                    debugPositionText = debugPositionText
                )


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
                UtilityActionsStrip(
                    onSearch = { selectedTab(1) },
                    onInfo = { selectedTab(2) }
                )

            }
        }
    }
}




