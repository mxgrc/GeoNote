package com.example.geonote.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempImageUri
            }
        }
    )
val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                val newUri = createImageUri(ctx)
                tempImageUri = newUri
                cameraLauncher.launch(newUri)
            }
        }
    )

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
            tags,
            imageUri?.toString()
        )
    }


    Scaffold(topBar = { TopAppBar(title = { Text("Nueva nota") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Foto de la nota",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }
            
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            val newUri = createImageUri(ctx)
                            tempImageUri = newUri
                            cameraLauncher.launch(newUri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Foto")
                }

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
