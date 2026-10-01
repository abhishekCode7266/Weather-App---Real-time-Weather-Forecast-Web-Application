package com.example.data

import com.example.model.FirestoreNote
import com.example.model.NotePriority
import com.example.model.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class FirebaseSimulatorRepository {

    private val _currentUser = MutableStateFlow(
        UserSession(
            uid = "usr_demo_${System.currentTimeMillis() % 1000}",
            email = "intern@developer.io",
            displayName = "Week 3 Developer",
            isLoggedIn = true,
            token = "jwt_auth_wk3_session"
        )
    )
    val currentUser: StateFlow<UserSession> = _currentUser.asStateFlow()

    private val initialNotes = listOf(
        FirestoreNote(
            id = "doc_101",
            title = "OpenWeatherMap API Setup",
            content = "Configured API key in secrets, tested HTTP GET with query params, parsed JSON response into Weather model.",
            category = "REST API",
            priority = NotePriority.HIGH,
            timestamp = System.currentTimeMillis() - 3600000 * 2
        ),
        FirestoreNote(
            id = "doc_102",
            title = "Local Session Persistence",
            content = "SharedPreferences helper class implemented with rememberMe boolean and lastSearchedCity string.",
            category = "Local Storage",
            priority = NotePriority.MEDIUM,
            timestamp = System.currentTimeMillis() - 3600000 * 5
        ),
        FirestoreNote(
            id = "doc_103",
            title = "Firebase Auth Flow & Rules",
            content = "Created Sign Up and Login screens with regex email validation and minimum 6-character password security.",
            category = "Firebase",
            priority = NotePriority.LOW,
            timestamp = System.currentTimeMillis() - 3600000 * 12
        ),
        FirestoreNote(
            id = "doc_104",
            title = "Realtime Firestore Streams",
            content = "Implemented StreamBuilder equivalent with reactive StateFlow for live CRUD synchronization across the app.",
            category = "Firestore",
            priority = NotePriority.HIGH,
            timestamp = System.currentTimeMillis() - 3600000 * 24
        )
    )

    private val _notesStream = MutableStateFlow<List<FirestoreNote>>(initialNotes)
    val notesStream: StateFlow<List<FirestoreNote>> = _notesStream.asStateFlow()

    suspend fun login(email: String, password: String): Result<UserSession> {
        delay(600) // Simulate network roundtrip
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Invalid email format. Please enter a valid address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
        }

        val session = UserSession(
            uid = "usr_${UUID.randomUUID().toString().take(8)}",
            email = trimmedEmail,
            displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
            isLoggedIn = true,
            token = "auth_tok_${System.currentTimeMillis()}"
        )
        _currentUser.value = session
        return Result.success(session)
    }

    suspend fun signUp(email: String, password: String): Result<UserSession> {
        delay(700)
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Invalid email format."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password is too weak. Must be at least 6 characters."))
        }
        if (trimmedEmail.equals("taken@example.com", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("The email address is already in use by another account."))
        }

        val session = UserSession(
            uid = "usr_${UUID.randomUUID().toString().take(8)}",
            email = trimmedEmail,
            displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
            isLoggedIn = true,
            token = "auth_tok_${System.currentTimeMillis()}"
        )
        _currentUser.value = session
        return Result.success(session)
    }

    fun logout() {
        _currentUser.value = UserSession(
            uid = "",
            email = "",
            displayName = "",
            isLoggedIn = false,
            token = null
        )
    }

    suspend fun addDocument(title: String, content: String, category: String, priority: NotePriority): Result<FirestoreNote> {
        delay(300)
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }

        val newDoc = FirestoreNote(
            id = "doc_${UUID.randomUUID().toString().take(6)}",
            title = title.trim(),
            content = content.trim(),
            category = if (category.isBlank()) "General" else category.trim(),
            priority = priority,
            timestamp = System.currentTimeMillis()
        )

        _notesStream.value = listOf(newDoc) + _notesStream.value
        return Result.success(newDoc)
    }

    suspend fun updateDocument(id: String, title: String, content: String, category: String, priority: NotePriority): Result<Unit> {
        delay(300)
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }

        _notesStream.value = _notesStream.value.map {
            if (it.id == id) {
                it.copy(
                    title = title.trim(),
                    content = content.trim(),
                    category = if (category.isBlank()) it.category else category.trim(),
                    priority = priority,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                it
            }
        }
        return Result.success(Unit)
    }

    suspend fun deleteDocument(id: String): Result<Unit> {
        delay(250)
        _notesStream.value = _notesStream.value.filter { it.id != id }
        return Result.success(Unit)
    }

    companion object {
        @Volatile
        private var instance: FirebaseSimulatorRepository? = null

        fun getInstance(): FirebaseSimulatorRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseSimulatorRepository().also { instance = it }
            }
        }
    }
}
