/// Hourly weather item for 24-hour forecast
class HourlyForecast {
  final String time;
  final double temperature;
  final String condition;
  final String iconCode;

  const HourlyForecast({
    required this.time,
    required this.temperature,
    required this.condition,
    required this.iconCode,
  });

  factory HourlyForecast.fromOpenMeteo({
    required String timeIso,
    required double temp,
    required int code,
  }) {
    String formattedTime = timeIso;
    try {
      final dt = DateTime.parse(timeIso);
      final hour = dt.hour;
      final ampm = hour >= 12 ? 'PM' : 'AM';
      final displayHour = hour % 12 == 0 ? 12 : hour % 12;
      formattedTime = '$displayHour $ampm';
    } catch (_) {}

    return HourlyForecast(
      time: formattedTime,
      temperature: temp,
      condition: _parseCondition(code),
      iconCode: _parseIconCode(code, true),
    );
  }

  static String _parseCondition(int code) {
    if (code == 0) return 'Clear';
    if (code <= 3) return 'Partly Cloudy';
    if (code <= 48) return 'Fog';
    if (code <= 67) return 'Rain';
    if (code <= 77) return 'Snow';
    if (code >= 95) return 'Thunderstorm';
    return 'Cloudy';
  }

  static String _parseIconCode(int code, bool isDay) {
    final suffix = isDay ? 'd' : 'n';
    if (code == 0) return '01$suffix';
    if (code == 1 || code == 2) return '02$suffix';
    if (code == 3) return '03$suffix';
    if (code <= 48) return '50$suffix';
    if (code <= 67) return '10$suffix';
    if (code <= 77) return '13$suffix';
    if (code >= 95) return '11$suffix';
    return '04$suffix';
  }
}

/// Daily forecast item for 7-day weather view
class DailyForecast {
  final String dayName;
  final String date;
  final double tempMin;
  final double tempMax;
  final String condition;
  final String iconCode;

  const DailyForecast({
    required this.dayName,
    required this.date,
    required this.tempMin,
    required this.tempMax,
    required this.condition,
    required this.iconCode,
  });

  factory DailyForecast.fromOpenMeteo({
    required String dateStr,
    required double minTemp,
    required double maxTemp,
    required int code,
    required int index,
  }) {
    String day = 'Day $index';
    try {
      final dt = DateTime.parse(dateStr);
      final days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
      day = index == 0 ? 'Today' : days[dt.weekday - 1];
    } catch (_) {}

    return DailyForecast(
      dayName: day,
      date: dateStr,
      tempMin: minTemp,
      tempMax: maxTemp,
      condition: HourlyForecast._parseCondition(code),
      iconCode: HourlyForecast._parseIconCode(code, true),
    );
  }
}

/// Comprehensive Weather Data Model
class WeatherModel {
  final String cityName;
  final String country;
  final double temperature;
  final double feelsLike;
  final int humidity;
  final String condition;
  final String description;
  final String iconCode;
  final double windSpeed;
  final double tempMin;
  final double tempMax;
  final int pressure;
  final bool isDay;
  final List<HourlyForecast> hourlyForecast;
  final List<DailyForecast> dailyForecast;

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
    this.isDay = true,
    this.hourlyForecast = const [],
    this.dailyForecast = const [],
  });

  /// Factory constructor to parse OpenWeatherMap 2.5 API JSON response.
  factory WeatherModel.fromOpenWeatherMapJson(Map<String, dynamic> json) {
    final weatherList = json['weather'] as List<dynamic>?;
    final weatherItem = (weatherList != null && weatherList.isNotEmpty)
        ? weatherList[0] as Map<String, dynamic>
        : null;

    final main = json['main'] as Map<String, dynamic>? ?? {};
    final wind = json['wind'] as Map<String, dynamic>? ?? {};
    final sys = json['sys'] as Map<String, dynamic>? ?? {};

    final temp = (main['temp'] as num?)?.toDouble() ?? 0.0;
    final feels = (main['feels_like'] as num?)?.toDouble() ?? temp;
    final tempMin = (main['temp_min'] as num?)?.toDouble() ?? temp;
    final tempMax = (main['temp_max'] as num?)?.toDouble() ?? temp;
    final icon = weatherItem?['icon'] as String? ?? '01d';

    return WeatherModel(
      cityName: json['name'] as String? ?? 'Unknown City',
      country: sys['country'] as String? ?? '',
      temperature: temp,
      feelsLike: feels,
      humidity: (main['humidity'] as num?)?.toInt() ?? 0,
      condition: weatherItem?['main'] as String? ?? 'Clear',
      description: (weatherItem?['description'] as String?)?.isNotEmpty == true
          ? _capitalize(weatherItem!['description'] as String)
          : 'Clear sky',
      iconCode: icon,
      windSpeed: ((wind['speed'] as num?)?.toDouble() ?? 0.0) * 3.6,
      tempMin: tempMin,
      tempMax: tempMax,
      pressure: (main['pressure'] as num?)?.toInt() ?? 1013,
      isDay: icon.endsWith('d'),
      hourlyForecast: [],
      dailyForecast: [],
    );
  }

  /// Backward-compatible alias for fromOpenWeatherMapJson
  factory WeatherModel.fromJson(Map<String, dynamic> json) =>
      WeatherModel.fromOpenWeatherMapJson(json);

  /// Factory constructor for Open-Meteo REST API (zero-config, high reliability).
  factory WeatherModel.fromOpenMeteoJson({
    required String cityName,
    required String country,
    required Map<String, dynamic> data,
  }) {
    final current = data['current'] as Map<String, dynamic>? ?? {};
    final hourly = data['hourly'] as Map<String, dynamic>? ?? {};
    final daily = data['daily'] as Map<String, dynamic>? ?? {};

    final temp = (current['temperature_2m'] as num?)?.toDouble() ?? 0.0;
    final feels = (current['apparent_temperature'] as num?)?.toDouble() ?? temp;
    final humidity = (current['relative_humidity_2m'] as num?)?.toInt() ?? 0;
    final wind = (current['wind_speed_10m'] as num?)?.toDouble() ?? 0.0;
    final pressure = (current['surface_pressure'] as num?)?.toInt() ?? 1013;
    final code = (current['weather_code'] as num?)?.toInt() ?? 0;
    final isDay = (current['is_day'] as num?)?.toInt() == 1;

    // Parse Hourly (next 24 hours)
    final List<HourlyForecast> hourlyList = [];
    final hourlyTimes = hourly['time'] as List<dynamic>? ?? [];
    final hourlyTemps = hourly['temperature_2m'] as List<dynamic>? ?? [];
    final hourlyCodes = hourly['weather_code'] as List<dynamic>? ?? [];

    final hourlyCount = hourlyTimes.length.clamp(0, 24);
    for (int i = 0; i < hourlyCount; i++) {
      hourlyList.add(HourlyForecast.fromOpenMeteo(
        timeIso: hourlyTimes[i].toString(),
        temp: (hourlyTemps[i] as num?)?.toDouble() ?? 0.0,
        code: (hourlyCodes[i] as num?)?.toInt() ?? 0,
      ));
    }

    // Parse Daily (next 7 days)
    final List<DailyForecast> dailyList = [];
    final dailyTimes = daily['time'] as List<dynamic>? ?? [];
    final dailyMaxTemps = daily['temperature_2m_max'] as List<dynamic>? ?? [];
    final dailyMinTemps = daily['temperature_2m_min'] as List<dynamic>? ?? [];
    final dailyCodes = daily['weather_code'] as List<dynamic>? ?? [];

    final dailyCount = dailyTimes.length.clamp(0, 7);
    double tempMin = temp;
    double tempMax = temp;

    for (int i = 0; i < dailyCount; i++) {
      final minT = (dailyMinTemps[i] as num?)?.toDouble() ?? temp;
      final maxT = (dailyMaxTemps[i] as num?)?.toDouble() ?? temp;
      if (i == 0) {
        tempMin = minT;
        tempMax = maxT;
      }
      dailyList.add(DailyForecast.fromOpenMeteo(
        dateStr: dailyTimes[i].toString(),
        minTemp: minT,
        maxTemp: maxT,
        code: (dailyCodes[i] as num?)?.toInt() ?? 0,
        index: i,
      ));
    }

    return WeatherModel(
      cityName: cityName,
      country: country,
      temperature: temp,
      feelsLike: feels,
      humidity: humidity,
      condition: HourlyForecast._parseCondition(code),
      description: _parseDescription(code),
      iconCode: HourlyForecast._parseIconCode(code, isDay),
      windSpeed: wind,
      tempMin: tempMin,
      tempMax: tempMax,
      pressure: pressure,
      isDay: isDay,
      hourlyForecast: hourlyList,
      dailyForecast: dailyList,
    );
  }

  static String _parseDescription(int code) {
    if (code == 0) return 'Clear Sky';
    if (code == 1) return 'Mainly Clear';
    if (code == 2) return 'Partly Cloudy';
    if (code == 3) return 'Overcast';
    if (code == 45 || code == 48) return 'Fog & Depositing Rime';
    if (code <= 55) return 'Light Drizzle';
    if (code <= 65) return 'Rain Showers';
    if (code <= 75) return 'Snow Fall';
    if (code >= 95) return 'Thunderstorm Activity';
    return 'Cloudy Sky';
  }

  static String _capitalize(String text) {
    if (text.isEmpty) return text;
    return text
        .split(' ')
        .map((w) => w.isNotEmpty ? '${w[0].toUpperCase()}${w.substring(1).toLowerCase()}' : '')
        .join(' ');
  }

  /// Get temperature formatted in Celsius or Fahrenheit
  String formattedTemp({bool isCelsius = true}) {
    if (isCelsius) {
      return '${temperature.round()}°C';
    }
    final f = (temperature * 9 / 5) + 32;
    return '${f.round()}°F';
  }

  /// Get feels like formatted
  String formattedFeelsLike({bool isCelsius = true}) {
    if (isCelsius) {
      return '${feelsLike.round()}°C';
    }
    final f = (feelsLike * 9 / 5) + 32;
    return '${f.round()}°F';
  }

  /// Icon URL from OpenWeatherMap CDN
  String get iconUrl => 'https://openweathermap.org/img/wn/$iconCode@2x.png';

  Map<String, dynamic> toJson() => {
    'cityName': cityName,
    'country': country,
    'temperature': temperature,
    'feelsLike': feelsLike,
    'humidity': humidity,
    'condition': condition,
    'description': description,
    'iconCode': iconCode,
    'windSpeed': windSpeed,
    'tempMin': tempMin,
    'tempMax': tempMax,
    'pressure': pressure,
  };
}

/// Convenience typedef for assignments referencing the class as 'Weather'.
typedef Weather = WeatherModel;
