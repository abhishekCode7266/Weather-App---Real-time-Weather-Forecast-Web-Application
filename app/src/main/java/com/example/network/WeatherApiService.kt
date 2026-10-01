package com.example.network

import com.example.model.GeocodingResponse
import com.example.model.OpenMeteoResponse
import com.example.model.OwmResponse
import com.example.model.WeatherData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeocodingApi {
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

interface MeteoWeatherApi {
    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,is_day",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}

interface OpenWeatherMapApi {
    @GET("https://api.openweathermap.org/data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OwmResponse
}

class WeatherRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val geocodingRetrofit = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val meteoRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val owmRetrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geocodingApi = geocodingRetrofit.create(GeocodingApi::class.java)
    private val meteoApi = meteoRetrofit.create(MeteoWeatherApi::class.java)
    private val owmApi = owmRetrofit.create(OpenWeatherMapApi::class.java)

    suspend fun fetchWeather(cityName: String, owmApiKey: String? = null): Result<WeatherData> = withContext(Dispatchers.IO) {
        val trimmed = cityName.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a city name"))
        }

        // If user provided a valid OWM API key, try OpenWeatherMap first
        if (!owmApiKey.isNullOrBlank()) {
            try {
                val owmResult = owmApi.getWeatherByCity(trimmed, owmApiKey)
                val weatherCondition = owmResult.weather.firstOrNull()?.main ?: "Clear"
                val weatherDesc = owmResult.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Clear sky"
                val icon = owmResult.weather.firstOrNull()?.icon ?: "01d"
                val country = owmResult.sys?.country ?: ""

                val weatherData = WeatherData(
                    cityName = owmResult.name,
                    country = country,
                    temperatureCelsius = owmResult.main.temp,
                    feelsLikeCelsius = owmResult.main.feels_like,
                    humidity = owmResult.main.humidity,
                    condition = weatherCondition,
                    conditionDescription = weatherDesc,
                    iconCode = icon,
                    windSpeedKph = (owmResult.wind?.speed ?: 0.0) * 3.6,
                    pressureHpa = owmResult.main.pressure ?: 1013,
                    isDay = !icon.endsWith("n"),
                    minTemp = owmResult.main.temp_min ?: (owmResult.main.temp - 2.0),
                    maxTemp = owmResult.main.temp_max ?: (owmResult.main.temp + 2.0)
                )
                return@withContext Result.success(weatherData)
            } catch (e: Exception) {
                // If OWM fails (e.g. 401 unauthorized or invalid key), fall back gracefully to Open-Meteo
                // but keep error message if it wasn't a fallback scenario
            }
        }

        // Open-Meteo public REST API (Geocoding -> Current Weather)
        try {
            val geoResponse = geocodingApi.searchCity(name = trimmed)
            val firstLocation = geoResponse.results?.firstOrNull()
                ?: return@withContext Result.failure(NoSuchElementException("City \"$trimmed\" not found. Please check spelling."))

            val weatherResp = meteoApi.getCurrentWeather(
                latitude = firstLocation.latitude,
                longitude = firstLocation.longitude
            )

            val current = weatherResp.current
                ?: return@withContext Result.failure(IllegalStateException("No current weather data available for ${firstLocation.name}"))

            val (condition, description, iconCode) = parseWeatherCode(current.weather_code, current.is_day == 1)
            val maxTemp = weatherResp.daily?.temperature_2m_max?.firstOrNull() ?: (current.temperature_2m + 3.0)
            val minTemp = weatherResp.daily?.temperature_2m_min?.firstOrNull() ?: (current.temperature_2m - 3.0)

            val data = WeatherData(
                cityName = firstLocation.name,
                country = firstLocation.country ?: "",
                temperatureCelsius = current.temperature_2m,
                feelsLikeCelsius = current.apparent_temperature,
                humidity = current.relative_humidity_2m,
                condition = condition,
                conditionDescription = description,
                iconCode = iconCode,
                windSpeedKph = current.wind_speed_10m,
                isDay = current.is_day == 1,
                minTemp = minTemp,
                maxTemp = maxTemp
            )
            Result.success(data)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "Network error. Please verify your internet connection."
                e is NoSuchElementException ->
                    e.message ?: "City not found."
                else ->
                    "Failed to fetch weather: ${e.localizedMessage ?: "Unknown error"}"
            }
            Result.failure(Exception(message, e))
        }
    }

    private fun parseWeatherCode(code: Int, isDay: Boolean): Triple<String, String, String> {
        val suffix = if (isDay) "d" else "n"
        return when (code) {
            0 -> Triple("Clear", if (isDay) "Sunny & Clear" else "Clear Night", "01$suffix")
            1 -> Triple("Mainly Clear", "Mostly Clear", "02$suffix")
            2 -> Triple("Partly Cloudy", "Partly Cloudy", "03$suffix")
            3 -> Triple("Overcast", "Overcast Skies", "04$suffix")
            45, 48 -> Triple("Fog", "Foggy & Mist", "50$suffix")
            51, 53, 55 -> Triple("Drizzle", "Light Drizzle", "09$suffix")
            56, 57 -> Triple("Freezing Drizzle", "Freezing Drizzle", "13$suffix")
            61, 63 -> Triple("Rain", "Moderate Rain", "10$suffix")
            65 -> Triple("Heavy Rain", "Heavy Rain Pouring", "10$suffix")
            66, 67 -> Triple("Freezing Rain", "Freezing Rain", "13$suffix")
            71, 73, 75 -> Triple("Snow", "Snowfall", "13$suffix")
            77 -> Triple("Snow Grains", "Snow Grains", "13$suffix")
            80, 81, 82 -> Triple("Rain Showers", "Passing Rain Showers", "09$suffix")
            85, 86 -> Triple("Snow Showers", "Snow Showers", "13$suffix")
            95 -> Triple("Thunderstorm", "Thunderstorm", "11$suffix")
            96, 99 -> Triple("Severe Storm", "Thunderstorm with Hail", "11$suffix")
            else -> Triple("Cloudy", "Variable Clouds", "03$suffix")
        }
    }
}
