package com.example.geonote.repository

import com.example.geonote.model.NoteDao
import com.example.geonote.model.NoteEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class NoteRepositoryTest {

    private lateinit var repository: NoteRepository
    private lateinit var mockDao: NoteDao

    @Before
    fun setup() {
        mockDao = mockk()
        repository = NoteRepository(mockDao)
    }

    @Test
    fun `getNotes retorna flujo de notas activas del DAO`() = runTest {
        // Given
        val expectedNotes = listOf(
            NoteEntity(1, "Nota 1", "Contenido 1"),
            NoteEntity(2, "Nota 2", "Contenido 2")
        )
        coEvery { mockDao.getActiveNotes() } returns flowOf(expectedNotes)

        // When
        val result = repository.getNotes()

        // Then
        result.collect { notes ->
            assertThat(notes).isEqualTo(expectedNotes)
            assertThat(notes.size).isEqualTo(2)
        }
        coVerify(exactly = 1) { mockDao.getActiveNotes() }
    }

    @Test
    fun `get retorna nota por id cuando existe`() = runTest {
        // Given
        val noteId = 1L
        val expectedNote = NoteEntity(noteId, "Test", "Content")
        coEvery { mockDao.getById(noteId) } returns expectedNote

        // When
        val result = repository.get(noteId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(noteId)
        assertThat(result?.title).isEqualTo("Test")
        coVerify(exactly = 1) { mockDao.getById(noteId) }
    }

    @Test
    fun `get retorna null cuando nota no existe`() = runTest {
        // Given
        val noteId = 999L
        coEvery { mockDao.getById(noteId) } returns null

        // When
        val result = repository.get(noteId)

        // Then
        assertThat(result).isNull()
        coVerify(exactly = 1) { mockDao.getById(noteId) }
    }

    @Test
    fun `save inserta nota y retorna su id`() = runTest {
        // Given
        val note = NoteEntity(0, "Nueva Nota", "Contenido")
        val expectedId = 5L
        coEvery { mockDao.upsert(note) } returns expectedId

        // When
        val resultId = repository.save(note)

        // Then
        assertThat(resultId).isEqualTo(expectedId)
        coVerify(exactly = 1) { mockDao.upsert(note) }
    }

    @Test
    fun `save actualiza nota existente`() = runTest {
        // Given
        val existingNote = NoteEntity(3, "Nota Existente", "Actualizada")
        coEvery { mockDao.upsert(existingNote) } returns 3L

        // When
        val resultId = repository.save(existingNote)

        // Then
        assertThat(resultId).isEqualTo(3L)
        coVerify(exactly = 1) { mockDao.upsert(existingNote) }
    }

    @Test
    fun `archive marca nota como archivada`() = runTest {
        // Given
        val noteId = 2L
        coEvery { mockDao.archive(noteId) } returns Unit

        // When
        repository.archive(noteId)

        // Then
        coVerify(exactly = 1) { mockDao.archive(noteId) }
    }

    @Test
    fun `delete elimina nota físicamente`() = runTest {
        // Given
        val note = NoteEntity(4, "Nota a Eliminar", "Contenido")
        coEvery { mockDao.delete(note) } returns Unit

        // When
        repository.delete(note)

        // Then
        coVerify(exactly = 1) { mockDao.delete(note) }
    }

    @Test
    fun `getNotes retorna flujo vacío cuando no hay notas`() = runTest {
        // Given
        coEvery { mockDao.getActiveNotes() } returns flowOf(emptyList())

        // When
        val result = repository.getNotes()

        // Then
        result.collect { notes ->
            assertThat(notes).isEmpty()
        }
    }
}
