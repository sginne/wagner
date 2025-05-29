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
import com.viivi.wagner.AppConfig
import com.viivi.wagner.utils.formatDate
import androidx.compose.ui.Alignment
import java.time.LocalDate




@Composable
fun SearchPage(comics: List<Comic>?, onSelect: (Comic) -> Unit) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }

    fun parseDate(text: String) = runCatching { LocalDate.parse(text) }.getOrNull()

    val startDate = parseDate(startDateText)
    val endDate = parseDate(endDateText)

    val filteredComics = comics?.filter {
        val date = it.publishedDate?.let { d -> runCatching { LocalDate.parse(d) }.getOrNull() }
        val matchesQuery = it.title.contains(query.text, ignoreCase = true)
        val inRange = (startDate == null || (date != null && date >= startDate)) &&
                (endDate == null || (date != null && date <= endDate))
        matchesQuery && inRange
    } ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = startDateText,
                onValueChange = { startDateText = it },
                label = { Text("Початкова дата (yyyy-MM-dd)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = endDateText,
                onValueChange = { endDateText = it },
                label = { Text("Кінцева дата (yyyy-MM-dd)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Пошук коміксів") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredComics) { comic ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onSelect(comic) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comic.title.removePrefix(AppConfig.prefix).trim(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = comic.publishedDate?.let { formatDate(it) } ?: "невідомо",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider()
            }
        }
    }
}
