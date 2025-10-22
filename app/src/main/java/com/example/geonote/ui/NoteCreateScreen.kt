package com.example.geonote.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.geonote.model.LocationProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreateScreen(
    onSave: (title: String, body: String, lat: Double?, lon: Double?, acc: Float?, tags: String?) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    var location by remember { mutableStateOf<Location?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Estado de permiso
    val hasFinePermission = remember {
        ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    var fineGranted by remember { mutableStateOf(hasFinePermission) }

    val requestFinePermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        fineGranted = granted
        if (!granted) {
            error = "Permiso de ubicación denegado. Puedes guardar la nota sin posición."
        }
    }

    fun askLocationPermission() {
        if (!fineGranted) requestFinePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun saveNow() {
        onSave(
            title,
            body,
            location?.latitude,
            location?.longitude,
            location?.accuracy,
            tags
        )
        onBack()
    }


    suspend fun fetchLocation() {
        try {
            loading = true
            val provider = LocationProvider(ctx)
            location = provider.getCurrentLocation()
            if (location == null) error = "No se pudo obtener ubicación."
        } catch (e: Exception) {
            error = e.message ?: "Error al obtener ubicación."
        } finally {
            loading = false
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Nueva nota") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Título") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = body, onValueChange = { body = it },
                label = { Text("Contenido") }, modifier = Modifier.fillMaxWidth(), minLines = 4
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = tags, onValueChange = { tags = it },
                label = { Text("Etiquetas") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row {
                Button(
                    onClick = {
                        if (!fineGranted) {
                            askLocationPermission()
                        } else {
                            scope.launch { fetchLocation() }
                        }
                    },
                    enabled = !loading
                ) {
                    Text(
                        when {
                            loading -> "Obteniendo…"
                            location != null -> "Ubicación lista ✓"
                            else -> "Obtener ubicación"
                        }
                    )
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { saveNow() },
                    enabled = title.isNotBlank() && !loading
                ) { Text("Guardar") }
            }

            if (loading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text("⚠️ $it", color = MaterialTheme.colorScheme.error)
            }

            location?.let {
                Spacer(Modifier.height(8.dp))
                Text("📍 ${it.latitude}, ${it.longitude} • ±${it.accuracy.toInt()} m")
            }
        }
    }
}
