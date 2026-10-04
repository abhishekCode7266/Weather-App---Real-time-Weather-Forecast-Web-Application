import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/weather_model.dart';

/// Exception thrown when weather data fetching or parsing fails.
class WeatherException implements Exception {
  final String message;
  final int? statusCode;

  WeatherException(this.message, [this.statusCode]);

  @override
  String toString() => message;
}

/// Service class responsible for fetching real-time weather and forecasts.
/// Supports Open-Meteo (zero-config, free, high reliability) and OpenWeatherMap REST APIs.
class WeatherService {
  static const String _owmBaseUrl = 'https://api.openweathermap.org/data/2.5/weather';
  static const String _geocodingBaseUrl = 'https://geocoding-api.open-meteo.com/v1/search';
  static const String _meteoForecastBaseUrl = 'https://api.open-meteo.com/v1/forecast';

  final String? apiKey;
  final http.Client _client;

  WeatherService({
    this.apiKey,
    http.Client? client,
  }) : _client = client ?? http.Client();

  /// Fetches weather data for [cityName].
  /// If a valid OpenWeatherMap API key is provided, it uses OpenWeatherMap.
  /// Otherwise, it uses Open-Meteo REST API which requires no key, has no rate limits,
  /// and provides current + 24h hourly + 7-day daily forecasts.
  Future<WeatherModel> fetchWeather(String cityName, {String? customApiKey}) async {
    final query = cityName.trim();
    if (query.isEmpty) {
      throw WeatherException('Please enter a valid city name.');
    }

    final effectiveApiKey = (customApiKey ?? apiKey ?? '').trim();

    // If an OpenWeatherMap API key is provided and valid, try OWM first
    if (effectiveApiKey.isNotEmpty) {
      try {
        final owmResult = await _fetchFromOpenWeatherMap(query, effectiveApiKey);
        return owmResult;
      } catch (e) {
        // Fall through to Open-Meteo if OWM fails
      }
    }

    // High-reliability default provider: Open-Meteo REST API
    return await _fetchFromOpenMeteo(query);
  }

  /// Alias for backward compatibility
  Future<WeatherModel> fetchWeatherByCity(String cityName) => fetchWeather(cityName);

  /// Fetch from Open-Meteo with geocoding + full forecast
  Future<WeatherModel> _fetchFromOpenMeteo(String query) async {
    try {
      // Step 1: Geocoding search
      final geoUri = Uri.parse(
        '$_geocodingBaseUrl?name=${Uri.encodeComponent(query)}&count=5&language=en&format=json',
      );

      final geoResponse = await _client.get(geoUri).timeout(const Duration(seconds: 10));

      if (geoResponse.statusCode != 200) {
        throw WeatherException('Could not connect to weather server (HTTP ${geoResponse.statusCode}).');
      }

      final Map<String, dynamic> geoData = json.decode(geoResponse.body) as Map<String, dynamic>;
      final results = geoData['results'] as List<dynamic>?;

      if (results == null || results.isEmpty) {
        throw WeatherException('City "$query" not found. Please check spelling.');
      }

      final firstMatch = results[0] as Map<String, dynamic>;
      final lat = firstMatch['latitude'];
      final lon = firstMatch['longitude'];
      final resolvedCity = firstMatch['name'] as String? ?? query;
      final country = firstMatch['country'] as String? ?? '';

      // Step 2: Fetch weather & forecast
      final weatherUri = Uri.parse(
        '$_meteoForecastBaseUrl?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,surface_pressure,wind_speed_10m&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto',
      );

      final weatherResponse = await _client.get(weatherUri).timeout(const Duration(seconds: 10));

      if (weatherResponse.statusCode != 200) {
        throw WeatherException('Failed to fetch weather for $resolvedCity (HTTP ${weatherResponse.statusCode}).');
      }

      final Map<String, dynamic> weatherData = json.decode(weatherResponse.body) as Map<String, dynamic>;

      return WeatherModel.fromOpenMeteoJson(
        cityName: resolvedCity,
        country: country,
        data: weatherData,
      );
    } on TimeoutException {
      throw WeatherException('Connection timed out. Please check your internet connection.');
    } on FormatException {
      throw WeatherException('Invalid response received from weather server.');
    } catch (e) {
      if (e is WeatherException) rethrow;
      throw WeatherException('Error loading weather: ${e.toString().replaceFirst("Exception: ", "")}');
    }
  }

  /// Fetch from OpenWeatherMap REST API
  Future<WeatherModel> _fetchFromOpenWeatherMap(String query, String key) async {
    final uri = Uri.parse(
      '$_owmBaseUrl?q=${Uri.encodeComponent(query)}&appid=$key&units=metric',
    );

    final response = await _client.get(uri).timeout(const Duration(seconds: 10));

    if (response.statusCode == 200) {
      final Map<String, dynamic> data = json.decode(response.body);
      return WeatherModel.fromOpenWeatherMapJson(data);
    } else if (response.statusCode == 404) {
      throw WeatherException('City "$query" not found on OpenWeatherMap.', 404);
    } else if (response.statusCode == 401) {
      throw WeatherException('Invalid OpenWeatherMap API key.', 401);
    } else {
      throw WeatherException('OpenWeatherMap returned HTTP ${response.statusCode}.', response.statusCode);
    }
  }

  /// Search city suggestions for autocomplete
  Future<List<String>> searchCities(String query) async {
    if (query.trim().length < 2) return [];
    try {
      final geoUri = Uri.parse(
        '$_geocodingBaseUrl?name=${Uri.encodeComponent(query.trim())}&count=5&language=en&format=json',
      );
      final response = await _client.get(geoUri).timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        final Map<String, dynamic> data = json.decode(response.body);
        final results = data['results'] as List<dynamic>?;
        if (results != null) {
          return results.map((r) {
            final name = r['name'] as String? ?? '';
            final country = r['country'] as String? ?? '';
            final admin1 = r['admin1'] as String? ?? '';
            if (admin1.isNotEmpty && country.isNotEmpty) {
              return '$name, $admin1, $country';
            } else if (country.isNotEmpty) {
              return '$name, $country';
            }
            return name;
          }).toList();
        }
      }
    } catch (_) {}
    return [];
  }

  void dispose() {
    _client.close();
  }
}
