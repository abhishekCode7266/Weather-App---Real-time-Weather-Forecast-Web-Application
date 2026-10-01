/// Data model representing the weather data parsed from OpenWeatherMap REST API.
class WeatherModel {
  /// City name returned by the API (e.g., "London", "Tokyo")
  final String cityName;

  /// Two-letter country code (e.g., "GB", "JP")
  final String country;

  /// Current temperature in Celsius
  final double temperature;

  /// Perceived temperature in Celsius
  final double feelsLike;

  /// Humidity percentage (0 - 100%)
  final int humidity;

  /// Main weather group/condition (e.g., "Clear", "Rain", "Clouds", "Snow")
  final String condition;

  /// Weather condition within the group with description (e.g., "light rain", "overcast clouds")
  final String description;

  /// Weather icon id code from OpenWeatherMap (e.g., "01d", "10n")
  final String iconCode;

  /// Wind speed in kilometers per hour (km/h)
  final double windSpeed;

  /// Minimum temperature recorded
  final double tempMin;

  /// Maximum temperature recorded
  final double tempMax;

  /// Atmospheric pressure in hPa
  final int pressure;

  const WeatherModel({
    required this.cityName,
    required this.country,
    required this.temperature,
    required this.feelsLike,
    required this.humidity,
    required this.condition,
    required this.description,
    required this.iconCode,
    required this.windSpeed,
    required this.tempMin,
    required this.tempMax,
    required this.pressure,
  });

  /// Factory constructor to parse OpenWeatherMap 2.5 API JSON response.
  /// Handles null values and numeric conversions gracefully.
  factory WeatherModel.fromJson(Map<String, dynamic> json) {
    // Weather condition array (weather[0])
    final weatherList = json['weather'] as List<dynamic>?;
    final weatherItem = (weatherList != null && weatherList.isNotEmpty)
        ? weatherList[0] as Map<String, dynamic>
        : null;

    // Main weather metrics (temperature, humidity, pressure, etc.)
    final main = json['main'] as Map<String, dynamic>? ?? {};

    // Wind metrics
    final wind = json['wind'] as Map<String, dynamic>? ?? {};

    // System metrics (country, sunrise, sunset)
    final sys = json['sys'] as Map<String, dynamic>? ?? {};

    return WeatherModel(
      cityName: json['name'] as String? ?? 'Unknown City',
      country: sys['country'] as String? ?? '',
      temperature: (main['temp'] as num?)?.toDouble() ?? 0.0,
      feelsLike: (main['feels_like'] as num?)?.toDouble() ?? 0.0,
      humidity: (main['humidity'] as num?)?.toInt() ?? 0,
      condition: weatherItem?['main'] as String? ?? 'Clear',
      description: (weatherItem?['description'] as String?)?.isNotEmpty == true
          ? _capitalize(weatherItem!['description'] as String)
          : 'Clear sky',
      iconCode: weatherItem?['icon'] as String? ?? '01d',
      // Convert m/s from OpenWeatherMap to km/h (* 3.6)
      windSpeed: ((wind['speed'] as num?)?.toDouble() ?? 0.0) * 3.6,
      tempMin: (main['temp_min'] as num?)?.toDouble() ?? 0.0,
      tempMax: (main['temp_max'] as num?)?.toDouble() ?? 0.0,
      pressure: (main['pressure'] as num?)?.toInt() ?? 1013,
    );
  }

  /// Converts the [WeatherModel] into a Map (JSON representation).
  Map<String, dynamic> toJson() {
    return {
      'name': cityName,
      'sys': {'country': country},
      'main': {
        'temp': temperature,
        'feels_like': feelsLike,
        'humidity': humidity,
        'temp_min': tempMin,
        'temp_max': tempMax,
        'pressure': pressure,
      },
      'weather': [
        {
          'main': condition,
          'description': description,
          'icon': iconCode,
        }
      ],
      'wind': {
        'speed': windSpeed / 3.6, // back to m/s
      },
    };
  }

  /// URL pointing to the official OpenWeatherMap condition icon.
  String get iconUrl => 'https://openweathermap.org/img/wn/$iconCode@2x.png';

  /// Returns temperature formatted with degree Celsius symbol (e.g., "24°C").
  String get formattedTemp => '${temperature.round()}°C';

  /// Temperature converted to Fahrenheit.
  double get temperatureFahrenheit => (temperature * 9 / 5) + 32;

  /// Returns temperature in Fahrenheit (e.g., "75°F").
  String get formattedTempFahrenheit => '${temperatureFahrenheit.round()}°F';

  /// Helper to capitalize the first letter of words in condition description.
  static String _capitalize(String text) {
    if (text.isEmpty) return text;
    return text
        .split(' ')
        .map((word) => word.isNotEmpty
            ? '${word[0].toUpperCase()}${word.substring(1).toLowerCase()}'
            : '')
        .join(' ');
  }

  @override
  String toString() {
    return 'WeatherModel(cityName: $cityName, temp: ${temperature.round()}°C, condition: $condition, humidity: $humidity%)';
  }
}

/// Convenience typedef for assignments referencing the class as 'Weather'.
typedef Weather = WeatherModel;
