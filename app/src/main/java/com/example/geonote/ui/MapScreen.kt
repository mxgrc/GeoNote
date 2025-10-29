package com.example.geonote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geonote.model.NoteEntity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(notes: List<NoteEntity>) {
    val notesWithLocation = remember(notes) {
        notes.filter { it.lat != null && it.lon != null }
    }

    val defaultLocation = LatLng(-33.4489, -70.6693) // Santiago, Chile

    val centerPosition = remember(notesWithLocation) {
        if (notesWithLocation.isNotEmpty()) {
            val firstNote = notesWithLocation.first()
            LatLng(firstNote.lat!!, firstNote.lon!!)
        } else {
            defaultLocation
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPosition, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Notas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (notesWithLocation.isEmpty()) {
                // Sin notas con ubicación
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🗺️ No hay notas con ubicación",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Crea notas y agrega ubicación para verlas aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Mapa con marcadores
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        compassEnabled = true
                    )
                ) {
                    notesWithLocation.forEach { note ->
                        val position = LatLng(note.lat!!, note.lon!!)

                        Marker(
                            state = MarkerState(position = position),
                            title = note.title,
                            snippet = note.body.take(60) + if (note.body.length > 60) "..." else ""
                        )
                    }
                }

                // Contador de notas en el mapa
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        "${notesWithLocation.size} ${if (notesWithLocation.size == 1) "nota" else "notas"}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}