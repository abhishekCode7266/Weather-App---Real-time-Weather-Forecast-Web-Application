import 'dart:async';
import 'dart:convert';
import 'dart:io';
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

/// Service class responsible for fetching weather data via HTTP from OpenWeatherMap.
class WeatherService {
  static const String _baseUrl = 'https://api.openweathermap.org/data/2.5/weather';

  /// OpenWeatherMap API Key.
  /// Can be passed in the constructor or configured via environment / constants.
  final String apiKey;
  final http.Client _client;

  WeatherService({
    this.apiKey = '',
    http.Client? client,
  }) : _client = client ?? http.Client();

  /// Asynchronously fetches current weather data for a given [cityName].
  ///
  /// - Throws [WeatherException] for empty queries, network dropouts, timeouts,
  ///   invalid city names (404), or unauthorized requests (401).
  Future<Weather> fetchWeather(String cityName) async {
    final query = cityName.trim();
    if (query.isEmpty) {
      throw WeatherException('Please enter a valid city name.');
    }

    final effectiveApiKey = apiKey.trim();
    if (effectiveApiKey.isEmpty) {
      throw WeatherException(
        'OpenWeatherMap API key is missing. Please configure your API key.',
      );
    }

    final Uri uri = Uri.parse(
      '$_baseUrl?q=${Uri.encodeComponent(query)}&appid=$effectiveApiKey&units=metric',
    );

    try {
      final http.Response response = await _client
          .get(uri)
          .timeout(const Duration(seconds: 12));

      return _processResponse(response, query);
    } on SocketException {
      throw WeatherException(
        'Network failure: No internet connection. Please check your network.',
      );
    } on TimeoutException {
      throw WeatherException(
        'Network failure: Connection timed out. The server took too long to respond.',
      );
    } on http.ClientException catch (e) {
      throw WeatherException('Network failure: ${e.message}');
    } on FormatException {
      throw WeatherException('Failed to parse API response: Invalid JSON format.');
    } catch (e) {
      if (e is WeatherException) rethrow;
      throw WeatherException('Unexpected error occurred: ${e.toString()}');
    }
  }

  /// Alias for [fetchWeather] for compatibility with different naming conventions.
  Future<Weather> fetchWeatherByCity(String cityName) => fetchWeather(cityName);

  /// Internal handler for the HTTP response status codes.
  Weather _processResponse(http.Response response, String query) {
    Map<String, dynamic> jsonBody;
    try {
      jsonBody = json.decode(response.body) as Map<String, dynamic>;
    } catch (_) {
      throw WeatherException('Failed to decode server response.', response.statusCode);
    }

    switch (response.statusCode) {
      case 200:
        return convertJsonToWeather(jsonBody);

      case 404:
        throw WeatherException(
          'City "$query" not found. Please verify the spelling and try again.',
          404,
        );

      case 401:
        throw WeatherException(
          'Unauthorized: Invalid or inactive OpenWeatherMap API key.',
          401,
        );

      case 429:
        throw WeatherException(
          'Too many requests: OpenWeatherMap API rate limit exceeded.',
          429,
        );

      case 500:
      case 502:
      case 503:
        throw WeatherException(
          'OpenWeatherMap server error. Please try again later.',
          response.statusCode,
        );

      default:
        final message = jsonBody['message'] as String? ?? 'HTTP error ${response.statusCode}';
        throw WeatherException('Failed to fetch weather: $message', response.statusCode);
    }
  }

  /// Converts the API JSON response map into a [Weather] model object.
  Weather convertJsonToWeather(Map<String, dynamic> json) {
    return WeatherModel.fromJson(json);
  }

  /// Frees up resources used by the HTTP client.
  void dispose() {
    _client.close();
  }
}
