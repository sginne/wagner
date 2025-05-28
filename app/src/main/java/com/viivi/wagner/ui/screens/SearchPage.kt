package com.viivi.wagner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.viivi.wagner.model.Comic
import androidx.compose.foundation.clickable


@Composable
fun SearchPage(comics: List<Comic>?, onSelect: (Comic) -> Unit) {
    var query by remember { mutableStateOf(TextFieldValue("")) }

    val filteredComics = comics?.filter {
        it.title.contains(query.text, ignoreCase = true)
    } ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Пошук коміксів") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredComics) { comic ->
                Text(
                    text = comic.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onSelect(comic) },
                    style = MaterialTheme.typography.bodyLarge
                )
                Divider()
            }
        }
    }
}
