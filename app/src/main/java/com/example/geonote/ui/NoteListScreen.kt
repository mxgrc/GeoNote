package com.example.geonote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.geonote.model.NoteEntity

@Composable
fun NoteListScreen(
    notes: List<NoteEntity>,
    onAdd: () -> Unit,
    onArchive: (Long) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin notas aún. Toca + para crear una 👇")
            }
        } else {
            LazyColumn(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { n ->
                    Card(Modifier.fillMaxWidth()) {
                        Column {
                            n.imageUri?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Foto de ${n.title}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(Modifier.padding(12.dp)) {
                                Text(n.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(n.body, maxLines = 2)
                                Spacer(Modifier.height(6.dp))
                                val pos = when {
                                    n.lat != null && n.lon != null ->
                                        "📍 ${n.lat.format(5)}, ${n.lon.format(5)}"
                                    else -> "📍 sin ubicación"
                                }
                                val acc = n.accuracy?.let { " • ±${it.toInt()} m" } ?: ""
                                Text("$pos$acc", style = MaterialTheme.typography.bodySmall)
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { onArchive(n.id) }) { Text("Archivar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
