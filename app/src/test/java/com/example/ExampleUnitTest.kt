package com.example

import com.example.data.FirebaseSimulatorRepository
import com.example.model.NotePriority
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAuthValidation_weakPassword() = runBlocking {
        val repo = FirebaseSimulatorRepository.getInstance()
        val result = repo.login("test@example.com", "123")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("at least 6 characters") == true)
    }

    @Test
    fun testAuthValidation_invalidEmail() = runBlocking {
        val repo = FirebaseSimulatorRepository.getInstance()
        val result = repo.login("invalid-email-string", "password123")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid email") == true)
    }

    @Test
    fun testFirestoreCrud_addAndStream() = runBlocking {
        val repo = FirebaseSimulatorRepository.getInstance()
        val initialSize = repo.notesStream.value.size

        val addResult = repo.addDocument(
            title = "Test Weather Note",
            content = "Tested OpenWeatherMap API",
            category = "Unit Test",
            priority = NotePriority.HIGH
        )

        assertTrue(addResult.isSuccess)
        assertEquals(initialSize + 1, repo.notesStream.value.size)

        val newDocId = addResult.getOrNull()!!.id
        val deleteResult = repo.deleteDocument(newDocId)
        assertTrue(deleteResult.isSuccess)
        assertEquals(initialSize, repo.notesStream.value.size)
    }
}

