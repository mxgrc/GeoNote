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

    val notes: StateFlow<List<NoteEntity>> =
        repo.getNotes().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun clearError() {
        _validationError.value = null
    }

    fun saveNote(
        title: String,
        body: String,
        lat: Double?,
        lon: Double?,
        accuracy: Float?,
        tags: String?,
        imageUri: String?
    ) {

        if (title.isBlank()) {
            _validationError.value = "El título no puede estar vacío"
            return
        }

        if (body.isBlank()) {
            _validationError.value = "El contenido no puede estar vacío"
            return
        }

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
                archived = false,
                imageUri = imageUri
            )
            repo.save(note)
            
            _saveSuccess.emit(Unit) 
        }
    }

    fun archive(id: Long) {
        viewModelScope.launch { repo.archive(id) }
    }
}
