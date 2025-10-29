package com.example.geonote.ui

import android.Manifest
import android.content.Context
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.geonote.model.LocationProvider
import com.example.geonote.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.io.File

fun createImageUri(context: Context): Uri {
    val imageDir = File(context.filesDir, "images")
    if (!imageDir.exists()) imageDir.mkdir()
    val file = File(imageDir, "IMG_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

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

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

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

    val validationError by viewModel.validationError.collectAsState()

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

    LaunchedEffect(key1 = Unit) {
        viewModel.saveSuccess.collect {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva nota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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
                Spacer(Modifier.height(8.dp))
            }

            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        viewModel.clearError()
                    },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationError?.contains("Título", true) == true,
                    leadingIcon = { Icon(Icons.Default.Title, null) }
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
                    minLines = 6,
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
                    label = { Text("Etiquetas (separadas por comas)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Tag, null) }
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
                                        locationError = null
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (location != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (location != null) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                            null,
                            Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(when {
                            loadingLocation -> "..."
                            location != null -> "✓"
                            else -> "Ubicación"
                        })
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
                        Spacer(Modifier.width(6.dp))
                        Text("Foto")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.saveNote(
                            title = title,
                            body = body,
                            lat = location?.latitude,
                            lon = location?.longitude,
                            accuracy = location?.accuracy,
                            tags = tags,
                            imageUri = imageUri?.toString()
                        )
                    },
                    enabled = !loadingLocation,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Nota", style = MaterialTheme.typography.labelLarge)
                }

                if (loadingLocation) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                }

                locationError?.let {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp)) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                location?.let {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Ubicación obtenida", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("${it.latitude.format(5)}, ${it.longitude.format(5)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("Precisión: ±${it.accuracy.toInt()} m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)