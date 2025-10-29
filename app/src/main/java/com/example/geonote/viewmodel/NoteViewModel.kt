package com.example.geonote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geonote.model.NoteEntity
import com.example.geonote.repository.NoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repo: NoteRepository
) : ViewModel() {

    // 1. Flujo para la lista de notas
    val notes: StateFlow<List<NoteEntity>> =
        repo.getNotes().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    // 2. Flujo para el error de validación
    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    // 3. Flujo para el evento de guardado exitoso (el que acabamos de agregar)
    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    /** Limpia el error de validación (para que la UI lo oculte) */
    fun clearError() {
        _validationError.value = null
    }

    /** Guarda la nota, validando los campos primero */
    fun saveNote(
        title: String,
        body: String,
        lat: Double?,
        lon: Double?,
        accuracy: Float?,
        tags: String?
    ) {

        // Lógica de Validación
        if (title.isBlank()) {
            _validationError.value = "El título no puede estar vacío"
            return
        }

        if (body.isBlank()) {
            _validationError.value = "El contenido no puede estar vacío"
            return
        }

        // Si la validación pasa, se guarda
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = NoteEntity(
                title = title.trim(),
                body = body.trim(),
                lat = lat,
                lon = lon,
                accuracy = accuracy,
                createdAt = now,
                updatedAt = now,
                tags = tags?.trim().takeUnless { it.isNullOrBlank() },
                archived = false
            )
            repo.save(note)
            
            // Avisa a la UI que se guardó exitosamente
            _saveSuccess.emit(Unit) 
        }
    }

    /** Archiva una nota */
    fun archive(id: Long) {
        viewModelScope.launch { repo.archive(id) }
    }
}
