package com.example.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeatherData
import com.example.network.WeatherRepository
import com.example.storage.AppPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WeatherScreen(
    weatherRepo: WeatherRepository,
    appPreferences: AppPreferences,
    modifier: Modifier = Modifier
) {
    val prefsState by appPreferences.state.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf(prefsState.lastSearchedCity) }
    var isLoading by remember { mutableStateOf(false) }
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var customApiKey by remember { mutableStateOf("") }

    val quickCities = listOf("London", "New York", "Tokyo", "Paris", "Sydney", "Mumbai", "Cairo")

    fun performSearch(cityToSearch: String) {
        val targetCity = cityToSearch.trim()
        if (targetCity.isEmpty()) {
            errorMessage = "Please enter a valid city name."
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            val result = weatherRepo.fetchWeather(
                cityName = targetCity,
                owmApiKey = customApiKey.ifBlank { null }
            )
            isLoading = false
            result.onSuccess { data ->
                weatherData = data
                errorMessage = null
                // Save last searched city as required by assignment 2
                appPreferences.setLastSearchedCity(data.cityName)
            }.onFailure { err ->
                errorMessage = err.message ?: "Failed to fetch weather data. Please check your connection."
                weatherData = null
            }
        }
    }

    // Load initial city on first composition
    LaunchedEffect(Unit) {
        val initialCity = prefsState.lastSearchedCity.ifBlank { "London" }
        searchQuery = initialCity
        performSearch(initialCity)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Assignment 1: Weather",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "OpenWeatherMap & Public REST API",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // OWM API Key Config button
            OutlinedButton(
                onClick = { showApiKeyDialog = true },
                modifier = Modifier.testTag("api_key_settings_button"),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Configure API Key",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (customApiKey.isBlank()) "API Key" else "Custom Key",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("city_search_input"),
            placeholder = { Text("Search city (e.g., Tokyo, London)...") },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { performSearch(searchQuery) },
                        modifier = Modifier.testTag("search_submit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Search / Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick City Filter Chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCities.forEach { city ->
                val isSelected = searchQuery.equals(city, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        searchQuery = city
                        performSearch(city)
                    },
                    label = { Text(city, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("chip_city_$city"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Unit Toggle & Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (customApiKey.isBlank()) "Source: Public Geocoding REST" else "Source: OpenWeatherMap API",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Unit: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { appPreferences.setTempUnitCelsius(!prefsState.tempUnitCelsius) },
                    modifier = Modifier.testTag("toggle_temp_unit_button")
                ) {
                    Text(
                        text = if (prefsState.tempUnitCelsius) "°C (Metric)" else "°F (Imperial)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Content: Loading, Error, or Weather Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("weather_loading_indicator"),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fetching live weather for $searchQuery...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (errorMessage != null) {
                // User-friendly error message card with retry
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weather_error_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Weather Search Notice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "An unexpected error occurred.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { performSearch(searchQuery) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.testTag("weather_retry_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Again")
                        }
                    }
                }
            } else if (weatherData != null) {
                val data = weatherData!!
                val isCelsius = prefsState.tempUnitCelsius
                val displayTemp = if (isCelsius) data.temperatureCelsius else (data.temperatureCelsius * 9 / 5 + 32)
                val displayFeelsLike = if (isCelsius) data.feelsLikeCelsius else (data.feelsLikeCelsius * 9 / 5 + 32)
                val displayMin = if (isCelsius) data.minTemp else (data.minTemp * 9 / 5 + 32)
                val displayMax = if (isCelsius) data.maxTemp else (data.maxTemp * 9 / 5 + 32)
                val unitSymbol = if (isCelsius) "°C" else "°F"

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Hero Weather Card with dynamic gradient
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("weather_main_card"),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = getWeatherGradient(data.condition, data.isDay)
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // City & Country
                                    Text(
                                        text = data.cityName + if (data.country.isNotBlank()) ", ${data.country}" else "",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = data.conditionDescription,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Dynamic Weather Graphic / Icon
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(90.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = getWeatherIcon(data.condition, data.isDay),
                                                contentDescription = data.condition,
                                                tint = Color.White,
                                                modifier = Modifier.size(54.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Temperature
                                    Text(
                                        text = "${displayTemp.toInt()}$unitSymbol",
                                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Feels like ${displayFeelsLike.toInt()}$unitSymbol  •  H: ${displayMax.toInt()}$unitSymbol  L: ${displayMin.toInt()}$unitSymbol",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Meteorological Details Grid (Humidity, Wind, Pressure, Condition)
                        Text(
                            text = "Weather Details & Atmosphere",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WeatherMetricCard(
                                icon = Icons.Default.WaterDrop,
                                title = "Humidity",
                                value = "${data.humidity}%",
                                subtitle = if (data.humidity > 65) "High Moisture" else "Normal",
                                modifier = Modifier.weight(1f)
                            )
                            WeatherMetricCard(
                                icon = Icons.Default.Air,
                                title = "Wind Speed",
                                value = "${data.windSpeedKph.toInt()} km/h",
                                subtitle = if (data.windSpeedKph > 25) "Breezy" else "Gentle",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WeatherMetricCard(
                                icon = Icons.Default.Thermostat,
                                title = "Barometer",
                                value = "${data.pressureHpa} hPa",
                                subtitle = "Atmospheric",
                                modifier = Modifier.weight(1f)
                            )
                            WeatherMetricCard(
                                icon = if (data.isDay) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                title = "Day / Night",
                                value = if (data.isDay) "Daylight" else "Night Time",
                                subtitle = if (data.isDay) "Solar cycle" else "Lunar phase",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
            }
        }
    }

    // OpenWeatherMap API Key Configuration Dialog
    if (showApiKeyDialog) {
        var tempKey by remember { mutableStateOf(customApiKey) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Text("OpenWeatherMap API Key")
            },
            text = {
                Column {
                    Text(
                        text = "The app automatically uses Open-Meteo & OpenStreetMap Geocoding REST API with zero configuration. If you have an OpenWeatherMap API key, you can enter it here to test OWM endpoints directly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("OpenWeatherMap AppId") },
                        placeholder = { Text("e.g. b6907d289e10d714a6e88b30761fae22") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        customApiKey = tempKey.trim()
                        showApiKeyDialog = false
                        performSearch(searchQuery)
                    }
                ) {
                    Text("Apply & Refresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WeatherMetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getWeatherGradient(condition: String, isDay: Boolean): List<Color> {
    if (!isDay) {
        return listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
    }
    return when (condition.lowercase()) {
        "clear" -> listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFFBBF24))
        "rain", "drizzle", "rain showers" -> listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF64748B))
        "thunderstorm", "severe storm" -> listOf(Color(0xFF312E81), Color(0xFF4C1D95), Color(0xFF1E1B4B))
        "snow", "snow showers" -> listOf(Color(0xFF475569), Color(0xFF94A3B8), Color(0xFFCBD5E1))
        else -> listOf(Color(0xFF0369A1), Color(0xFF0EA5E9), Color(0xFF38BDF8))
    }
}

private fun getWeatherIcon(condition: String, isDay: Boolean): ImageVector {
    return when (condition.lowercase()) {
        "clear" -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
        "rain", "drizzle", "rain showers" -> Icons.Default.WaterDrop
        "thunderstorm", "severe storm" -> Icons.Default.WarningAmber
        "snow" -> Icons.Default.Cloud
        else -> Icons.Default.Cloud
    }
}
