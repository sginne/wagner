package com.norsula.wagner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UtilityActionsStrip(
    onSearch: () -> Unit,
    onInfo: () -> Unit
) {
    StationaryStrip {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onSearch) {
                Text("Пошук")
            }
            TextButton(onClick = onInfo) {
                Text("Інфо")
            }
        }
    }
}
