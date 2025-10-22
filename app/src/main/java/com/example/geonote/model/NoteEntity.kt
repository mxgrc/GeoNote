package com.example.geonote.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para una nota geolocalizada.
 * - lat/lon/accuracy pueden ser null si el usuario guarda sin ubicación.
 * - createdAt/updatedAt en millis para evitar converters.
 * - tags en CSV simple para MVP (sin TypeConverter).
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val body: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val accuracy: Float? = null,            // precisión en metros
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: String? = null,               // ejemplo: "viaje,trabajo,café"
    val archived: Boolean = false
)
