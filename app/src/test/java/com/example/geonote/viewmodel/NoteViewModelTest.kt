package com.example.geonote.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.geonote.model.NoteEntity
import com.example.geonote.repository.NoteRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: NoteViewModel
    private lateinit var mockRepository: NoteRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()

        // Mock para getNotes que siempre retorna un flow
        coEvery { mockRepository.getNotes() } returns flowOf(emptyList())

        viewModel = NoteViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== TESTS DE VALIDACIÓN ==========

    @Test
    fun `saveNote falla cuando título está vacío`() = runTest {
        // When
        viewModel.saveNote("", "Contenido válido", null, null, null, null, null)
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.validationError.value).isEqualTo("El título no puede estar vacío")
        coVerify(exactly = 0) { mockRepository.save(any()) }
    }

    @Test
    fun `saveNote falla cuando título solo tiene espacios`() = runTest {
        // When
        viewModel.saveNote("   ", "Contenido válido", null, null, null, null, null)
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.validationError.value).isEqualTo("El título no puede estar vacío")
    }

    @Test
    fun `saveNote falla cuando contenido está vacío`() = runTest {
        // When
        viewModel.saveNote("Título válido", "", null, null, null, null, null)
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.validationError.value).isEqualTo("El contenido no puede estar vacío")
    }

    @Test
    fun `saveNote falla cuando contenido solo tiene espacios`() = runTest {
        // When
        viewModel.saveNote("Título válido", "   ", null, null, null, null, null)
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.validationError.value).isEqualTo("El contenido no puede estar vacío")
    }

    // ========== TESTS DE GUARDADO ==========

    @Test
    fun `saveNote exitoso crea nueva nota con datos correctos`() = runTest {
        // Given
        coEvery { mockRepository.save(any()) } returns 1L

        // When
        viewModel.saveNote(
            title = "Mi Nota",
            body = "Contenido de prueba",
            lat = -33.4489,
            lon = -70.6693,
            accuracy = 10.5f,
            tags = "tag1,tag2",
            imageUri = "content://image.jpg"
        )
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.validationError.value).isNull()
        coVerify {
            mockRepository.save(match { note ->
                note.title == "Mi Nota" &&
                        note.body == "Contenido de prueba" &&
                        note.lat == -33.4489 &&
                        note.lon == -70.6693 &&
                        note.accuracy == 10.5f &&
                        note.tags == "tag1,tag2" &&
                        note.imageUri == "content://image.jpg" &&
                        !note.archived
            })
        }
    }

    @Test
    fun `saveNote recorta espacios del título y contenido`() = runTest {
        // Given
        coEvery { mockRepository.save(any()) } returns 1L

        // When
        viewModel.saveNote(
            title = "  Título con espacios  ",
            body = "  Contenido con espacios  ",
            lat = null,
            lon = null,
            accuracy = null,
            tags = null,
            imageUri = null
        )
        testScheduler.advanceUntilIdle()

        // Then
        coVerify {
            mockRepository.save(match { note ->
                note.title == "Título con espacios" &&
                        note.body == "Contenido con espacios"
            })
        }
    }

    @Test
    fun `saveNote maneja tags vacíos correctamente`() = runTest {
        // Given
        coEvery { mockRepository.save(any()) } returns 1L

        // When
        viewModel.saveNote("Título", "Contenido", null, null, null, "   ", null)
        testScheduler.advanceUntilIdle()

        // Then
        coVerify {
            mockRepository.save(match { note ->
                note.tags == null
            })
        }
    }

    @Test
    fun `saveNote crea nota sin ubicación cuando lat y lon son null`() = runTest {
        // Given
        coEvery { mockRepository.save(any()) } returns 1L

        // When
        viewModel.saveNote("Título", "Contenido", null, null, null, null, null)
        testScheduler.advanceUntilIdle()

        // Then
        coVerify {
            mockRepository.save(match { note ->
                note.lat == null && note.lon == null && note.accuracy == null
            })
        }
    }

    // ========== TESTS DE EDICIÓN ==========

    @Test
    fun `saveNote actualiza nota existente cuando noteId es válido`() = runTest {
        // Given
        val existingNote = NoteEntity(5L, "Original", "Original", createdAt = 1000L)
        coEvery { mockRepository.get(5L) } returns existingNote
        coEvery { mockRepository.save(any()) } returns 5L

        viewModel.loadNote(5L)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.saveNote(
            title = "Actualizado",
            body = "Contenido Actualizado",
            lat = null,
            lon = null,
            accuracy = null,
            tags = null,
            imageUri = null,
            noteId = 5L
        )
        testScheduler.advanceUntilIdle()

        // Then
        coVerify {
            mockRepository.save(match { note ->
                note.id == 5L &&
                        note.title == "Actualizado" &&
                        note.body == "Contenido Actualizado" &&
                        note.createdAt == 1000L // Mantiene fecha de creación original
            })
        }
    }

    @Test
    fun `loadNote carga nota correctamente`() = runTest {
        // Given
        val expectedNote = NoteEntity(3L, "Test", "Content")
        coEvery { mockRepository.get(3L) } returns expectedNote

        // When
        viewModel.loadNote(3L)
        testScheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.currentNote.value).isEqualTo(expectedNote)
        coVerify { mockRepository.get(3L) }
    }

    @Test
    fun `clearCurrentNote limpia la nota actual`() = runTest {
        // Given
        val note = NoteEntity(2L, "Test", "Content")
        coEvery { mockRepository.get(2L) } returns note
        viewModel.loadNote(2L)
        testScheduler.advanceUntilIdle()

        // When
        viewModel.clearCurrentNote()

        // Then
        assertThat(viewModel.currentNote.value).isNull()
    }

    // ========== TESTS DE ARCHIVADO ==========

    @Test
    fun `archive llama al repositorio correctamente`() = runTest {
        // Given
        coEvery { mockRepository.archive(10L) } returns Unit

        // When
        viewModel.archive(10L)
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { mockRepository.archive(10L) }
    }

    // ========== TESTS DE LIMPIEZA DE ERRORES ==========

    @Test
    fun `clearError limpia el error de validación`() = runTest {
        // Given
        viewModel.saveNote("", "Contenido", null, null, null, null, null)
        testScheduler.advanceUntilIdle()
        assertThat(viewModel.validationError.value).isNotNull()

        // When
        viewModel.clearError()

        // Then
        assertThat(viewModel.validationError.value).isNull()
    }

    // ========== TESTS DE CASOS EDGE ==========

    @Test
    fun `saveNote con noteId 0 crea nueva nota en lugar de actualizar`() = runTest {
        // Given
        coEvery { mockRepository.save(any()) } returns 1L

        // When
        viewModel.saveNote("Título", "Contenido", null, null, null, null, null, noteId = 0L)
        testScheduler.advanceUntilIdle()

        // Then
        coVerify {
            mockRepository.save(match { note ->
                note.id == 0L // Nueva nota
            })
        }
    }

    @Test
    fun `saveNote preserva todos los datos de ubicación cuando están presentes`() = runTest {
        // Given
        coEvery { mockRepository.save(any()) } returns 1L

        // When
        viewModel.saveNote(
            "Título",
            "Contenido",
            lat = -33.4489,
            lon = -70.6693,
            accuracy = 15.2f,
            tags = null,
            imageUri = null
        )
        testScheduler.advanceUntilIdle()

        // Then
        coVerify {
            mockRepository.save(match { note ->
                note.lat == -33.4489 &&
                        note.lon == -70.6693 &&
                        note.accuracy == 15.2f
            })
        }
    }
}
