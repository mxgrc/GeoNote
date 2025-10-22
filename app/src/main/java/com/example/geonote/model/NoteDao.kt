package com.example.geonote.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para manejar notas en la base de datos.
 * Todas las operaciones son suspend o Flow (para usarse con coroutines).
 */
@Dao
interface NoteDao {

    // Obtener todas las notas activas, ordenadas por fecha más reciente
    @Query("SELECT * FROM notes WHERE archived = 0 ORDER BY updatedAt DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    // Buscar una nota por ID
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    // Insertar o actualizar nota (Room detecta si existe)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    // Marcar como archivada
    @Query("UPDATE notes SET archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    // Borrar completamente (no usado en MVP, pero útil)
    @Delete
    suspend fun delete(note: NoteEntity)
}
