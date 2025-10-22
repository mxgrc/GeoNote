package com.example.geonote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geonote.model.NoteEntity
import com.example.geonote.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel expone la lista de notas y operaciones de guardar/archivar.
 */
class NoteViewModel(
    private val repo: NoteRepository
) : ViewModel() {

    /** Flujo con estado para que Compose observe cambios automáticamente */
    val notes: StateFlow<List<NoteEntity>> =
        repo.getNotes().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    /** Guarda una nueva nota o actualiza si ya existe */
    fun saveNote(
        title: String,
        body: String,
        lat: Double?,
        lon: Double?,
        accuracy: Float?,
        tags: String?
    ) {
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
        }
    }

    /** Archiva una nota por id */
    fun archive(id: Long) {
        viewModelScope.launch { repo.archive(id) }
    }
}
