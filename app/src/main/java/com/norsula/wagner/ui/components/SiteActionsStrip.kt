package com.norsula.wagner.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.norsula.wagner.model.Comic
import com.norsula.wagner.utils.formatDate

@Composable
fun SiteActionsStrip(
    comic: Comic,
    debugPositionText: String?
) {
    val context = LocalContext.current

    StationaryStrip {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = comic.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "переклад від ${
                            comic.publishedDate?.let { formatDate(it) }
                                ?: "невідомо"
                        }",
                        style = MaterialTheme.typography.bodySmall
                    )

                    debugPositionText?.let {
                        Text(
                            text = " • $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
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
                        Text("Переглянути на сайті")
                    }

                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, comic.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Подивись цей комікс: ${comic.title}\n" +
                                        "https://norsula.com/${comic.id}"
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Поділитися через"
                                )
                            )
                        }
                    ) {
                        Text("Поділитися")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .width(1.dp)
                    .height(58.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant
                    )
            )

            WagnerBannerAd()
        }
    }
}
