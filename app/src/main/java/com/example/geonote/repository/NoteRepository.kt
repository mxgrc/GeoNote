package com.example.geonote.repository

import com.example.geonote.model.NoteDao
import com.example.geonote.model.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de notas.
 * Aísla a la UI/ViewModel del detalle de persistencia (Room).
 */
class NoteRepository(
    private val dao: NoteDao
) {

    /** Flujo de notas activas ordenadas por fecha de actualización (desc) */
    fun getNotes(): Flow<List<NoteEntity>> = dao.getActiveNotes()

    /** Obtiene una nota por id (o null si no existe) */
    suspend fun get(id: Long): NoteEntity? = dao.getById(id)

    /** Inserta o actualiza una nota, devolviendo su id */
    suspend fun save(note: NoteEntity): Long = dao.upsert(note)

    /** Archiva (soft-delete) una nota */
    suspend fun archive(id: Long) = dao.archive(id)

    /** Elimina físicamente una nota (no usado en MVP, pero útil para mantenimiento) */
    suspend fun delete(note: NoteEntity) = dao.delete(note)
}
