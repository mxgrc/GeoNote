package com.example.geonote.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geonote.model.AppDatabase
import com.example.geonote.repository.NoteRepository

/**
 * Factory para crear NoteViewModel con sus dependencias (DB/DAO/Repo).
 * Úsala en tu Activity: by viewModels { NoteVMFactory(application) }
 */
class NoteVMFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(app)
        val repo = NoteRepository(db.noteDao())
        return NoteViewModel(repo) as T
    }
}
