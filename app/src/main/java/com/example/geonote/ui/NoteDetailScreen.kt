package com.example.geonote.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.geonote.model.LocationProvider
import com.example.geonote.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentNote by viewModel.currentNote.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<Location?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    var loadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val validationError by viewModel.validationError.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) imageUri = tempImageUri
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            val newUri = createImageUri(ctx)
            tempImageUri = newUri
            cameraLauncher.launch(newUri)
        }
    }

    val hasFinePermission = remember {
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    var fineGranted by remember { mutableStateOf(hasFinePermission) }

    val requestFinePermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        fineGranted = granted
        if (!granted) locationError = "Permiso denegado"
    }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    LaunchedEffect(currentNote) {
        currentNote?.let { note ->
            title = note.title
            body = note.body
            tags = note.tags ?: ""
            if (note.lat != null && note.lon != null) {
                location = Location("").apply {
                    latitude = note.lat
                    longitude = note.lon
                    accuracy = note.accuracy ?: 0f
                }
            }
            imageUri = note.imageUri?.let { Uri.parse(it) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            isEditing = false
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar nota" else "Detalle de nota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Foto de la nota",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(16.dp)) {
                if (isEditing) {
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
                        Text(
                            validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = body,
                        onValueChange = {
                            body = it
                            viewModel.clearError()
                        },
                        label = { Text("Contenido") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        isError = validationError?.contains("Contenido", true) == true
                    )
                    if (validationError?.contains("Contenido", true) == true) {
                        Text(
                            validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Etiquetas") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!fineGranted) {
                                    requestFinePermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                } else {
                                    scope.launch {
                                        try {
                                            loadingLocation = true
                                            val provider = LocationProvider(ctx)
                                            location = provider.getCurrentLocation()
                                            if (location == null) locationError = "No se pudo obtener ubicación"
                                        } catch (e: Exception) {
                                            locationError = e.message ?: "Error al obtener ubicación"
                                        } finally {
                                            loadingLocation = false
                                        }
                                    }
                                }
                            },
                            enabled = !loadingLocation,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (location != null) "Actualizar" else "Ubicación")
                        }

                        Button(
                            onClick = {
                                if (hasCameraPermission) {
                                    val newUri = createImageUri(ctx)
                                    tempImageUri = newUri
                                    cameraLauncher.launch(newUri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Foto")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                viewModel.saveNote(
                                    title = title,
                                    body = body,
                                    lat = location?.latitude,
                                    lon = location?.longitude,
                                    accuracy = location?.accuracy,
                                    tags = tags,
                                    imageUri = imageUri?.toString(),
                                    noteId = noteId
                                )
                            },
                            enabled = !loadingLocation,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Guardar")
                        }
                    }

                    if (loadingLocation) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }

                    locationError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("⚠️ $it", color = MaterialTheme.colorScheme.error)
                    }

                } else {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (tags.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Etiquetas: $tags",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    location?.let {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "📍 ${it.latitude.format(5)}, ${it.longitude.format(5)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Precisión: ±${it.accuracy.toInt()} m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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