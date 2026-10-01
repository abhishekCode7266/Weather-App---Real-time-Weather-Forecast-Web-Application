package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherData(
    val cityName: String,
    val country: String = "",
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val humidity: Int,
    val condition: String,
    val conditionDescription: String,
    val iconCode: String,
    val windSpeedKph: Double,
    val pressureHpa: Int = 1013,
    val isDay: Boolean = true,
    val minTemp: Double = temperatureCelsius - 3.0,
    val maxTemp: Double = temperatureCelsius + 4.0
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val apparent_temperature: Double,
    val weather_code: Int,
    val wind_speed_10m: Double,
    val is_day: Int
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    val temperature_2m_max: List<Double>? = null,
    val temperature_2m_min: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val current: OpenMeteoCurrent?,
    val daily: OpenMeteoDaily? = null
)

// OpenWeatherMap DTOs if user configures OWM API key
@JsonClass(generateAdapter = true)
data class OwmMain(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int,
    val temp_min: Double? = null,
    val temp_max: Double? = null,
    val pressure: Int? = null
)

@JsonClass(generateAdapter = true)
data class OwmWeather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class OwmWind(
    val speed: Double
)

@JsonClass(generateAdapter = true)
data class OwmSys(
    val country: String? = null
)

@JsonClass(generateAdapter = true)
data class OwmResponse(
    val name: String,
    val main: OwmMain,
    val weather: List<OwmWeather>,
    val wind: OwmWind?,
    val sys: OwmSys?
)
