package com.example.geonote.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos principal de la app GeoNote.
 * - Contiene la tabla "notes" (NoteEntity)
 * - Expone el DAO NoteDao para acceder a los datos.
 */
@Database(entities = [NoteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Acceso al DAO
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Devuelve una instancia única (singleton) de la base de datos.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geonote.db" // nombre del archivo local
                ).build().also { INSTANCE = it }
            }
        }
    }
}
