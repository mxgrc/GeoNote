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
import com.example.geonote.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreateScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    var location by remember { mutableStateOf<Location?>(null) }
    var loadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val validationError by viewModel.validationError.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.saveSuccess.collect {
            onBack()
        }
    }

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
            locationError = "Permiso denegado. Puedes guardar sin posición."
        }
    }

    fun askLocationPermission() {
        if (!fineGranted) requestFinePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    suspend fun fetchLocation() {
        try {
            loadingLocation = true
            val provider = LocationProvider(ctx)
            location = provider.getCurrentLocation()
            if (location == null) locationError = "No se pudo obtener ubicación."
        } catch (e: Exception) {
            locationError = e.message ?: "Error al obtener ubicación."
        } finally {
            loadingLocation = false
        }
    }

    fun saveNow() {
        viewModel.saveNote(
            title,
            body,
            location?.latitude,
            location?.longitude,
            location?.accuracy,
            tags
        )
    }


    Scaffold(topBar = { TopAppBar(title = { Text("Nueva nota") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    viewModel.clearError()
                },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationError?.contains("Título", true) == true
            )
            if (validationError?.contains("Título", true) == true) {
                Text(validationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = body,
                onValueChange = {
                    body = it
                    viewModel.clearError()
                },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                isError = validationError?.contains("Contenido", true) == true
            )
            if (validationError?.contains("Contenido", true) == true) {
                Text(validationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

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
                    enabled = !loadingLocation
                ) {
                    Text(
                        when {
                            loadingLocation -> "Obteniendo…"
                            location != null -> "Ubicación lista ✓"
                            else -> "Obtener ubicación"
                        }
                    )
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { saveNow() },
                    enabled = !loadingLocation
                ) { Text("Guardar") }
            }

            if (loadingLocation) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            locationError?.let {
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
