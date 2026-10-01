package com.example.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class PreferencesState(
        val rememberMe: Boolean,
        val lastSearchedCity: String,
        val tempUnitCelsius: Boolean,
        val darkModeEnabled: Boolean,
        val savedUserEmail: String
    )

    private val _state = MutableStateFlow(readCurrentState())
    val state: StateFlow<PreferencesState> = _state.asStateFlow()

    private fun readCurrentState(): PreferencesState {
        return PreferencesState(
            rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false),
            lastSearchedCity = prefs.getString(KEY_LAST_CITY, "London") ?: "London",
            tempUnitCelsius = prefs.getBoolean(KEY_UNIT_CELSIUS, true),
            darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false),
            savedUserEmail = prefs.getString(KEY_SAVED_EMAIL, "") ?: ""
        )
    }

    private fun syncState() {
        _state.value = readCurrentState()
    }

    fun setRememberMe(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply()
        syncState()
    }

    fun setLastSearchedCity(city: String) {
        if (city.isNotBlank()) {
            prefs.edit().putString(KEY_LAST_CITY, city.trim()).apply()
            syncState()
        }
    }

    fun setTempUnitCelsius(celsius: Boolean) {
        prefs.edit().putBoolean(KEY_UNIT_CELSIUS, celsius).apply()
        syncState()
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        syncState()
    }

    fun setSavedUserEmail(email: String) {
        prefs.edit().putString(KEY_SAVED_EMAIL, email).apply()
        syncState()
    }

    fun updateAll(rememberMe: Boolean, city: String, unitCelsius: Boolean, email: String) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .putString(KEY_LAST_CITY, city.trim())
            .putBoolean(KEY_UNIT_CELSIUS, unitCelsius)
            .putString(KEY_SAVED_EMAIL, email.trim())
            .apply()
        syncState()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        syncState()
    }

    fun getAllEntries(): Map<String, Any?> {
        return prefs.all
    }

    companion object {
        private const val PREFS_NAME = "week3_flutter_demo_prefs"
        const val KEY_REMEMBER_ME = "remember_me"
        const val KEY_LAST_CITY = "last_searched_city"
        const val KEY_UNIT_CELSIUS = "unit_is_celsius"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_SAVED_EMAIL = "saved_user_email"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
