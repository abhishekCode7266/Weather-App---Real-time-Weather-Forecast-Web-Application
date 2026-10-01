/// Data model representing weather information parsed from OpenWeatherMap.
class WeatherModel {
  final String cityName;
  final String country;
  final double temperature;
  final double feelsLike;
  final int humidity;
  final String condition;
  final String description;
  final String iconCode;
  final double windSpeedKph;
  final double minTemp;
  final double maxTemp;
  final int pressure;

  WeatherModel({
    required this.cityName,
    required this.country,
    required this.temperature,
    required this.feelsLike,
    required this.humidity,
    required this.condition,
    required this.description,
    required this.iconCode,
    required this.windSpeedKph,
    required this.minTemp,
    required this.maxTemp,
    required this.pressure,
  });

  /// Factory constructor to parse OpenWeatherMap 2.5 API JSON response
  factory WeatherModel.fromJson(Map<String, dynamic> json) {
    final weatherList = json['weather'] as List<dynamic>?;
    final weatherItem = weatherList != null && weatherList.isNotEmpty
        ? weatherList[0] as Map<String, dynamic>
        : null;

    final main = json['main'] as Map<String, dynamic>? ?? {};
    final wind = json['wind'] as Map<String, dynamic>? ?? {};
    final sys = json['sys'] as Map<String, dynamic>? ?? {};

    return WeatherModel(
      cityName: json['name'] as String? ?? 'Unknown',
      country: sys['country'] as String? ?? '',
      temperature: (main['temp'] as num?)?.toDouble() ?? 0.0,
      feelsLike: (main['feels_like'] as num?)?.toDouble() ?? 0.0,
      humidity: (main['humidity'] as num?)?.toInt() ?? 0,
      condition: weatherItem?['main'] as String? ?? 'Clear',
      description: weatherItem?['description'] as String? ?? 'Clear sky',
      iconCode: weatherItem?['icon'] as String? ?? '01d',
      windSpeedKph: ((wind['speed'] as num?)?.toDouble() ?? 0.0) * 3.6,
      minTemp: (main['temp_min'] as num?)?.toDouble() ?? 0.0,
      maxTemp: (main['temp_max'] as num?)?.toDouble() ?? 0.0,
      pressure: (main['pressure'] as num?)?.toInt() ?? 1013,
    );
  }

  /// Full URL for OpenWeatherMap condition icon (e.g. 10d@2x.png)
  String get iconUrl => 'https://openweathermap.org/img/wn/$iconCode@2x.png';
}

/// Alias for assignments specifying the 'Weather' model name.
typedef Weather = WeatherModel;

