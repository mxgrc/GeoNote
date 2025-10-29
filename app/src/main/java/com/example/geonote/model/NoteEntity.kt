package com.example.geonote.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val body: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val accuracy: Float? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: String? = null,
    val archived: Boolean = false,
    val imageUri: String? = null
)
