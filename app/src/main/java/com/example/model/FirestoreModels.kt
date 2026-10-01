package com.example.model

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val isLoggedIn: Boolean,
    val token: String? = null
)

data class FirestoreNote(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val priority: NotePriority = NotePriority.MEDIUM,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotePriority {
    LOW, MEDIUM, HIGH
}
