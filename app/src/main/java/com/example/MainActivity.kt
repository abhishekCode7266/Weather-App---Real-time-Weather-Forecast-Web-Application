package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.FirebaseSimulatorRepository
import com.example.network.NewsRepository
import com.example.network.WeatherRepository
import com.example.storage.AppPreferences
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var appPreferences: AppPreferences
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var newsRepository: NewsRepository
    private lateinit var firebaseRepository: FirebaseSimulatorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appPreferences = AppPreferences.getInstance(this)
        weatherRepository = WeatherRepository()
        newsRepository = NewsRepository()
        firebaseRepository = FirebaseSimulatorRepository.getInstance()

        setContent {
            val prefsState by appPreferences.state.collectAsState()
            val useDark = prefsState.darkModeEnabled || isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp(
                        weatherRepo = weatherRepository,
                        newsRepo = newsRepository,
                        appPreferences = appPreferences,
                        firebaseRepo = firebaseRepository
                    )
                }
            }
        }
    }
}
